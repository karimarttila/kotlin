package simpleserver.domaindb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.*


const val packageName = "domaindb"
val logger: Logger = LoggerFactory.getLogger(packageName)
const val infoMsg = "index.html => Info in HTML format"

sealed class ProductGroups
data class ProductGroupsFound(val data: List<Array<String>>) : ProductGroups()
object ProductGroupsNotFound : ProductGroups()

sealed class Products
data class ProductsFound(val data: List<Array<String>>) : Products()
object ProductsNotFound : Products()

sealed class Product
data class ProductFound(val data: Array<String>) : Product()
object ProductNotFound : Product()


fun getInfo(): String {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return infoMsg
}

fun getProductGroups(): Map<String, String> {
    logger.debug(L_ENTER)
    val pgData = readCsv("product-groups.csv")
    val ret = HashMap<String, String>()
    when (pgData) {
        is CsvDataFound -> pgData.data.forEach { ret[it[0]] = it[1] }
    }
    logger.debug(L_EXIT)
    return ret
}

fun getProducts(pgId: Int): Products {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"

    val ret = when(val csvData = readCsv(fileName)) {
        is CsvDataNotFound -> ProductsNotFound
        is CsvDataFound -> ProductsFound(csvData.data)
    }
    logger.debug(L_EXIT)
    return ret
}

fun getProduct(pgId: Int, pId: Int): Product {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"
    val ret = when(val pgData = readCsv(fileName)) {
        is CsvDataNotFound -> ProductNotFound
        is CsvDataFound ->
            when (val p = pgData.data.firstOrNull { it[0].equals(pId.toString()) }) {
                null -> ProductNotFound
                p -> ProductFound(arrayOf(p[0], p[1], p[2], p[3]))
                else -> ProductNotFound
            }
    }
    logger.debug(L_EXIT)
    return ret
}
