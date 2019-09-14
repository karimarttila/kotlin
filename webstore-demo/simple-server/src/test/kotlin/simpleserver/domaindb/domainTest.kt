package simpleserver.domaindb

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT


const val packageName = "domainTest"
val logger: Logger = LoggerFactory.getLogger(packageName)

class DomainTest {
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
        assertEquals("Books", productGroups["1"])
        assertEquals("Movies", productGroups["2"])
        logger.debug(L_EXIT)
    }

    @Test
    fun getProductsTest() {
        logger.debug(L_ENTER)
        when (val books = getProducts(1)) {
            is RawDataNotFound -> assertTrue(books is RawDataFound) // Fails for certain if not found.
            is RawDataFound -> assertEquals(35, books.data.size)
        }
        when (val movies = getProducts(2)) {
            is RawDataNotFound -> assertTrue(movies is RawDataFound)
            is RawDataFound -> {
                assertEquals(169, movies.data.size)
                val product = movies.data[48]
                assertEquals("Once Upon a Time in the West", product[2])
            }
        }
        val noSuchProductGroup = getProducts(3)
        assertTrue(noSuchProductGroup is RawDataNotFound)
        logger.debug(L_EXIT)
    }


}
