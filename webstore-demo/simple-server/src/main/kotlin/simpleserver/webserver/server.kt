package simpleserver.webserver

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
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
import io.ktor.http.content.default
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import org.slf4j.event.Level
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import java.nio.file.Paths


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
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
        allowCredentials = true
        anyHost()
    }

    install(Authentication) {
    }

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
        logger.debug(L_ENTER)

        // http://localhost:5065/info
        get("/info") {
            call.respondText("{\"info\":\"index.html => Info in HTML format\"}\n", contentType = ContentType.Text.Plain)
        }
        // http://localhost:5065/
        static("/") {
            // ******** NOTE! *********
            // When running under IDEA make sure that working directory is set to resources directory,
            // e.g. /mnt/edata/aw/kari/github/kotlin/webstore-demo/simple-server/src/main/resources
            defaultResource("static/index.html")
            //default("static/index.html")
        }
        // http://localhost:5065/index.html
        static("/index.html") {
            defaultResource("static/index.html")
            //default("static/index.html")
        }
        logger.debug(L_EXIT)
    }
}

    // TODO: NOT WORKING YET... CONTINUE HERE...
// http://localhost:5065/sign-in
    @Location("/sign-in")
    class SignIn()


