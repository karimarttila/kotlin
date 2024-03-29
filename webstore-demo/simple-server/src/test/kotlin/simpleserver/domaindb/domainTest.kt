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
        when (val productGroupsResult = getProductGroups()) {
            is ProductGroupsNotFound -> assertTrue(false) // Fails for certain if not found.
            is ProductGroupsFound -> {
                val pg = productGroupsResult.data
                assertEquals(2, pg.size)
                assertEquals("Books", pg["1"])
                assertEquals("Movies", pg["2"])
            }
        }
        logger.debug(L_EXIT)
    }

    @Test
    fun getProductsTest() {
        logger.debug(L_ENTER)
        when (val booksResult = getProducts(1)) {
            is ProductsNotFound -> assertTrue(false) // Fails for certain if not found.
            is ProductsFound -> assertEquals(35, booksResult.data.size)
        }
        when (val moviesResult = getProducts(2)) {
            is ProductsNotFound -> assertTrue(moviesResult is ProductsFound)
            is ProductsFound -> {
                val movies = moviesResult.data
                assertEquals(169, movies.size)
                val product = movies[48]
                assertEquals("Once Upon a Time in the West", product.title)
            }
        }
        val noSuchProductGroup = getProducts(3)
        assertTrue(noSuchProductGroup is ProductsNotFound)
        logger.debug(L_EXIT)
    }

    @Test
    fun getProductTest() {
        logger.debug(L_ENTER)
        val testMovie = Product(2, 49, "Once Upon a Time in the West", 14.4, "Leone, Sergio", 1968, "Italy-USA", "Western")
        when (val movieFound = getProduct(2, 49)) {
            is ProductNotFound -> assertTrue(false) // Fails for certain if not found.
            is ProductFound -> assertTrue(movieFound.data == testMovie)
        }
        val wrongProductGroupId = getProduct(5, 49)
        assertTrue(wrongProductGroupId is ProductNotFound)
        val wrongProductId = getProduct(2, 1000)
        assertTrue(wrongProductId is ProductNotFound)
        logger.debug(L_EXIT)
    }


}
