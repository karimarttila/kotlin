package simpleserver.domaindb

import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import kotlin.test.assertEquals

val packageName = "domainTest"
val logger: Logger = LoggerFactory.getLogger(packageName)

class TestSource {
    @Test
    fun getInfoTest() {
        logger.debug(L_ENTER)
        assertEquals("index.html => Info in HTML format", getInfo())
        logger.debug(L_EXIT)
    }

    @Test
    fun getProductGroupsTest() {
        logger.debug(L_ENTER)
        val productGroups = getProductGroups()
        assertEquals(2, productGroups.size)
        assertEquals("Books", productGroups.get("1"));
        assertEquals("Movies", productGroups.get("2"));
        logger.debug(L_EXIT)
    }


}
