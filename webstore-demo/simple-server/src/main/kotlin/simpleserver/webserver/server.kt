package simpleserver.webserver

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.jetty.Jetty
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.slf4j.event.Level
import simpleserver.domaindb.*
import simpleserver.userdb.NewUser
import simpleserver.userdb.UserAddError
import simpleserver.userdb.addUser
import simpleserver.userdb.checkCredentials
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT

sealed class SigninParamsResult
data class SigninParamsResultFound(val data: SigninPostData) : SigninParamsResult()
object SigninParamsResultNotFound : SigninParamsResult()

data class SigninPostData(
    val email: String,
    @JsonProperty("first-name") val firstName: String,
    @JsonProperty("last-name") val lastName: String,
    val password: String
)

sealed class LoginParamsResult
data class LoginParamsResultFound(val data: LoginPostData) : LoginParamsResult()
object LoginParamsResultNotFound : LoginParamsResult()

data class LoginPostData(
    val email: String,
    val password: String
)


private fun getStatusCode(response: Map<String, Any>): HttpStatusCode {
    return if (response["ret"] == "ok") HttpStatusCode.OK else HttpStatusCode.BadRequest
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private fun handleSigning(params: SigninParamsResult): Map<String, String> {
    logger.debug(L_ENTER)
    val ret = when (params) {
        is SigninParamsResultNotFound -> mapOf("ret" to "failed", "msg" to "Validation failed - some fields were empty")
        is SigninParamsResultFound -> {
            when (val newUser =
                addUser(params.data.email, params.data.firstName, params.data.lastName, params.data.password)) {
                is NewUser -> mapOf("ret" to "ok", "email" to newUser.data.email)
                is UserAddError -> mapOf("ret" to "failed", "msg" to newUser.msg)
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

private fun handleLogin(params: LoginParamsResult): Map<String, String> {
    logger.debug(L_ENTER)
    val ret = when (params) {
        is LoginParamsResultNotFound -> mapOf("ret" to "failed", "msg" to "Validation failed - some fields were empty")
        is LoginParamsResultFound -> {
            when (val credentialsOk = checkCredentials(params.data.email, params.data.password)) {
                false -> mapOf(
                    "ret" to "failed",
                    "msg" to "Credentials are not good - either email or password is not correct"
                )
                true -> {
                    val jwt = createJsonWebToken(params.data.email)
                    mapOf("ret" to "ok", "msg" to "Credentials ok", "json-web-token" to jwt)
                }
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

private fun handleProductGroups(token: ValidatedJwtResult): Map<String, Any> {
    logger.debug(L_ENTER)
    val ret = when (token) {
        is ValidatedJwtNotFound -> mapOf("ret" to "failed", "msg" to token.msg)
        is ValidatedJwtFound -> {
            val productGroups = getProductGroups()
            when (productGroups) {
                is ProductGroupsNotFound -> mapOf(
                    "ret" to "failed", "msg" to "Internal error: Product groups not found"
                )
                is ProductGroupsFound -> {
                    mapOf("ret" to "ok", "product-groups" to productGroups.data)
                }
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

private fun handleProducts(token: ValidatedJwtResult, pgId: String?): Map<String, Any> {
    logger.debug(L_ENTER)
    val ret = when (token) {
        is ValidatedJwtNotFound -> mapOf("ret" to "failed", "msg" to token.msg)
        is ValidatedJwtFound -> {
            val products = if (pgId == null) ProductsNotFound
            else try {
                getProducts(pgId.toInt())
            } catch (e: NumberFormatException) {
                ProductsNotFound
            }
            when (products) {
                is ProductsNotFound -> mapOf(
                    "ret" to "failed", "msg" to "Products not found for product group: $pgId"
                )
                is ProductsFound -> {
                    val productsList = products.data.map {
                        listOf(
                            it.pId.toString(),
                            it.pgId.toString(),
                            it.title,
                            it.price.toString()
                        )
                    }
                    // pgId cannot be null here, compiler misses the check above.
                    mapOf("ret" to "ok", "pg-id" to pgId!!, "products" to productsList)
                }
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

private fun handleProduct(token: ValidatedJwtResult, pgId: String?, pId: String?): Map<String, Any> {
    logger.debug(L_ENTER)
    val ret = when (token) {
        is ValidatedJwtNotFound -> mapOf("ret" to "failed", "msg" to token.msg)
        is ValidatedJwtFound -> {
            val product = when {
                pgId == null || pId == null -> ProductNotFound
                else -> {
                    try {
                        getProduct(pgId.toInt(), pId.toInt())
                    } catch (e: java.lang.NumberFormatException) {
                        ProductNotFound
                    }
                }
            }
            when (product) {
                is ProductNotFound -> mapOf(
                    "ret" to "failed", "msg" to "Product not found for product group: $pgId and product id: $pId"
                )
                is ProductFound -> mapOf(
                    "ret" to "ok", "pg-id" to pgId!!, "p-id" to pId!!, "product" to listOf(
                        product.data.pId.toString(),
                        product.data.pgId.toString(),
                        product.data.title,
                        product.data.price.toString(),
                        product.data.authorOrDirector,
                        product.data.year.toString(),
                        product.data.country,
                        product.data.genreOrLanguage
                    )
                )
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.main() {

    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.ContentType)
        //allowCredentials = true
        anyHost()
    }

    // NOTE: You should use Ktor's internal authentication.
    // The reason I didn't use in this exercise is that I mostly just implemented the session handling
    // manually as in previous exercises.
    //    install(Authentication) {
    //    }


    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Jetty) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    routing {
        // http://localhost:5065/info
        get("/info") {
            logger.debug(L_ENTER)
            call.respondText(
                """{"info":"index.html => Info in HTML format"}""",
                contentType = ContentType.Text.Plain
            )
            logger.debug(L_EXIT)
        }
        // http://localhost:5065/
        static("/") {
            logger.debug(L_ENTER)
            // ******** NOTE! *********
            // When running under IDEA make sure that working directory is set to resources directory,
            // e.g. /mnt/edata/aw/kari/github/kotlin/webstore-demo/simple-server/src/main/resources
            defaultResource("static/index.html")
            //default("static/index.html")
            logger.debug(L_EXIT)
        }
        // http://localhost:5065/index.html
        static("/index.html") {
            logger.debug(L_ENTER)
            defaultResource("static/index.html")
            logger.debug(L_EXIT)
        }
        post("/signin") {
            logger.debug(L_ENTER)
            val params = try {
                SigninParamsResultFound(call.receive<SigninPostData>())
            } catch (e: MissingKotlinParameterException) {
                SigninParamsResultNotFound
            }
            val response = handleSigning(params)
            val statusCode = getStatusCode(response)
            call.respond(statusCode, response)
            logger.debug(L_EXIT)
        }
        post("/login") {
            logger.debug(L_ENTER)
            val params = try {
                LoginParamsResultFound(call.receive<LoginPostData>())
            } catch (e: MissingKotlinParameterException) {
                LoginParamsResultNotFound
            }
            val response = handleLogin(params)
            val statusCode = getStatusCode(response)
            call.respond(statusCode, response)
            logger.debug(L_EXIT)
        }
        get("/product-groups") {
            logger.debug(L_ENTER)
            val token = validateJsonWebToken(call.request.header(HttpHeaders.Authorization))
            val response = handleProductGroups(token)
            val statusCode = getStatusCode(response)
            call.respond(statusCode, response)
            logger.debug(L_EXIT)
        }
        get("/products/{pgId}") {
            logger.debug(L_ENTER)
            val pgId = call.parameters["pgId"]
            val token = validateJsonWebToken(call.request.header(HttpHeaders.Authorization))
            val response = handleProducts(token, pgId)
            val statusCode = getStatusCode(response)
            call.respond(statusCode, response)
            logger.debug(L_EXIT)
        }
        get("/product/{pgId}/{pId}") {
            logger.debug(L_ENTER)
            val pgId = call.parameters["pgId"]
            val pId = call.parameters["pId"]
            val token = validateJsonWebToken(call.request.header(HttpHeaders.Authorization))
            val response = handleProduct(token, pgId, pId)
            val statusCode = getStatusCode(response)
            call.respond(statusCode, response)
            logger.debug(L_EXIT)
        }
    }
}


