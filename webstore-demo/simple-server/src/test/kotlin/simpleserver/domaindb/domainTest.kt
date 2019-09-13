package simpleserver.domaindb

import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

    @Test
    fun getProductsTest() {
        logger.debug(L_ENTER)
        val books = getProducts(1)
        assertNotNull(books)
        assertEquals(35, books.size)
        val movies = getProducts(2)
        assertNotNull(movies)
        assertEquals(169, movies.size)
        val product = movies[48]
        assertEquals("Once Upon a Time in the West", product[2]);
        logger.debug(L_EXIT)
    }


}
