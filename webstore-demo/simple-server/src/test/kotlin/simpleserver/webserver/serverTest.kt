package simpleserver.webserver

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT


class ServerTest {
    @Test
    fun getInfoTest() {
        logger.debug(L_ENTER)
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/info").apply {
                assertEquals("{\"info\":\"index.html => Info in HTML format\"}", response.content.toString().trim())
            }
            logger.debug(L_EXIT)
        }
    }
}