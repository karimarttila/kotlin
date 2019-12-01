package simpleserver.webserver

import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT

const val packageName = "sessionTest"
val logger: Logger = LoggerFactory.getLogger(packageName)

class SessionTest {

    @KtorExperimentalAPI
    @Test
    fun jwtTest() {
        logger.debug(L_ENTER)
        // First try to create it.
        val testEmail = "kari.karttinen@foo.com"
        val jwt = createJsonWebToken(testEmail)
        logger.debug("Returned jwt: $jwt")
        assertTrue(jwt.length > 10)
        // Now try to parse it.
        when (val ret = validateJsonWebToken(jwt)) {
            is ValidatedJwtNotFound -> {
                logger.error("Test failed, returned validatedJwtNotFound: ${ret.msg}")
                assertTrue(false) // Fails for certain if not found.
            }
            is ValidatedJwtFound -> {
                val parsedJwt = ret.data
                assertEquals(testEmail, parsedJwt)
            }
        }
        logger.debug(L_EXIT)
    }
}
