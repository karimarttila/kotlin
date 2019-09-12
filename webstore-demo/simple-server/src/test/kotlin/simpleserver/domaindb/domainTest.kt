package simpleserver.domaindb

import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import kotlin.test.assertEquals

val packageName = "domainTest"

class TestSource {
    @Test
    fun getInfoTest() {
        val logger: Logger = LoggerFactory.getLogger(packageName)
        logger.debug(L_ENTER)
        assertEquals("TODO", getInfo())
        logger.debug(L_EXIT)
    }
}
