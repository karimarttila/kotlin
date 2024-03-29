package simpleserver.webserver

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.withCharset
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.netty.handler.codec.http.HttpHeaders.addHeader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import simpleserver.userdb.initializeUserDb
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import java.util.*
import kotlin.collections.ArrayList


class ServerTest {

    @BeforeEach
    fun setup() {
        initializeUserDb()
    }

    @Test
    fun getInfoTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/info").apply {
                assertEquals("""{"info":"index.html => Info in HTML format"}""", response.content.toString())
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun postSigninMissingFieldsTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Post, "/signin") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                // email missing.
                setBody("""{"first-name":"Jamppa", "last-name":"Jamppanen", "password": "JampanSalasana"}""")
            }.apply {
                assertEquals(400, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "failed",
                                "msg" to "Validation failed - some fields were empty"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun postSigninTwiceFailedTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            // First call ok.
            handleRequest(HttpMethod.Post, "/signin") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"jamppa.jamppanen@foo.com", "first-name":"Jamppa", "last-name":"Jamppanen", "password": "JampanSalasana"}""")
            }.apply {
                assertEquals(200, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "ok",
                                "email" to "jamppa.jamppanen@foo.com"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            // Second call fails.
            handleRequest(HttpMethod.Post, "/signin") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"jamppa.jamppanen@foo.com", "first-name":"Jamppa", "last-name":"Jamppanen", "password": "JampanSalasana"}""")
            }.apply {
                assertEquals(400, response.status()?.value)
                // TODO
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "failed",
                                "msg" to "Email already exists: jamppa.jamppanen@foo.com"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }

            logger.debug(L_EXIT)
        }
    }

    @Test
    fun postSigninOkTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Post, "/signin") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"jamppa.jamppanen@foo.com", "first-name":"Jamppa", "last-name":"Jamppanen", "password": "JampanSalasana"}""")
            }.apply {
                assertEquals(200, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "ok",
                                "email" to "jamppa.jamppanen@foo.com"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun postLoginFailedTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"kari.karttinen@foo.com", "password": "WRONG-PASSWORD"}""")
            }.apply {
                assertEquals(400, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "failed",
                                "msg" to "Credentials are not good - either email or password is not correct"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun postLoginOkTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"kari.karttinen@foo.com", "password": "Kari"}""")
            }.apply {
                assertEquals(200, response.status()?.value)
                val resultMap = Gson().fromJson(response.content, Map::class.java)
                assertEquals("ok", resultMap["ret"])
                assertEquals("Credentials ok", resultMap["msg"])
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun getProductGroupsNoJwtTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/product-groups") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
            }.apply {
                assertEquals(400, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "failed",
                                "msg" to "No token"
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            logger.debug(L_EXIT)
        }
    }


    // TODO: Refactor the jwt handling in test cases into common utility function.
    @Test
    fun getProductGroupsTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            val jwtReq = handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"kari.karttinen@foo.com", "password": "Kari"}""")
            }
            val jwtResultMap = Gson().fromJson(jwtReq.response.content, Map::class.java)
            val jwt = jwtResultMap["json-web-token"] as String
            val encodedJwt = Base64.getEncoder().encodeToString(jwt.toByteArray())

            handleRequest(HttpMethod.Get, "/product-groups") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Authorization, "Basic $encodedJwt")
            }.apply {
                assertEquals(200, response.status()?.value)
                val mapper = ObjectMapper()
                assertEquals(
                    mapper.readTree(
                        Gson().toJson(
                            mapOf(
                                "ret" to "ok",
                                "product-groups" to mapOf("1" to "Books", "2" to "Movies")
                            )
                        )
                    ), mapper.readTree(response.content)
                )
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun getProductsTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            val jwtReq = handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"kari.karttinen@foo.com", "password": "Kari"}""")
            }
            val jwtResultMap = Gson().fromJson(jwtReq.response.content, Map::class.java)
            val jwt = jwtResultMap["json-web-token"] as String
            val encodedJwt = Base64.getEncoder().encodeToString(jwt.toByteArray())

            handleRequest(HttpMethod.Get, "/products/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Authorization, "Basic $encodedJwt")
            }.apply {
                assertEquals(200, response.status()?.value)
                val resultMap = Gson().fromJson(response.content, Map::class.java)
                assertEquals("ok", resultMap["ret"])
                assertEquals("1", resultMap["pg-id"])
                assertEquals(35, (resultMap["products"] as ArrayList<*>).size)
                val firstBook = (resultMap["products"] as ArrayList<*>)[0]
                assertEquals("2001", (firstBook as ArrayList<String>)[0])
                assertEquals("1", (firstBook as ArrayList<String>)[1])
                assertEquals("Kalevala", (firstBook as ArrayList<String>)[2])
                assertEquals("3.95", (firstBook as ArrayList<String>)[3])
            }
            logger.debug(L_EXIT)
        }
    }

    @Test
    fun getProductTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            val jwtReq = handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                setBody("""{"email":"kari.karttinen@foo.com", "password": "Kari"}""")
            }
            val jwtResultMap = Gson().fromJson(jwtReq.response.content, Map::class.java)
            val jwt = jwtResultMap["json-web-token"] as String
            val encodedJwt = Base64.getEncoder().encodeToString(jwt.toByteArray())

            handleRequest(HttpMethod.Get, "/product/2/49") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                addHeader(HttpHeaders.Authorization, "Basic $encodedJwt")
            }.apply {
                assertEquals(200, response.status()?.value)
                val resultMap = Gson().fromJson(response.content, Map::class.java)
                assertEquals("ok", resultMap["ret"])
                assertEquals("2", resultMap["pg-id"])
                assertEquals("49", resultMap["p-id"])
                assertEquals(8, (resultMap["product"] as ArrayList<*>).size)
                // What a coincidence! The chosen movie is the best western of all times!
                assertEquals("Once Upon a Time in the West", (resultMap["product"] as ArrayList<String>)[2])
            }
            logger.debug(L_EXIT)
        }
    }

}