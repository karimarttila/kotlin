package simpleserver.domaindb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.*


const val packageName = "domaindb"
val logger: Logger = LoggerFactory.getLogger(packageName)
const val infoMsg = "index.html => Info in HTML format"


typealias ProductGroups = HashMap<String, String>

sealed class ProductGroupsResult
data class ProductGroupsFound(val data: ProductGroups) : ProductGroupsResult()
object ProductGroupsNotFound : ProductGroupsResult()

data class Product(val pgId: Int, val pId: Int, val title: String, val price: Double,
                   val authorOrDirector: String, val year: Int, val country: String,
                   val genreOrLanguage: String)

sealed class ProductsResult
data class ProductsFound(val data: List<Product>) : ProductsResult()
object ProductsNotFound : ProductsResult()

sealed class ProductResult
data class ProductFound(val data: Product) : ProductResult()
object ProductNotFound : ProductResult()


fun getInfo(): String {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return infoMsg
}

fun getProductGroups(): ProductGroupsResult {
    logger.debug(L_ENTER)
    val fileName = "product-groups.csv"
    val ret = when (val csvData = readCsv(fileName)) {
        is CsvDataNotFound -> ProductGroupsNotFound
        is CsvDataFound -> {
            val pg = ProductGroups()
            csvData.data.forEach { pg[it[0]] = it[1] }
            ProductGroupsFound(pg)
        }
    }
    logger.debug(L_EXIT)
    return ret
}

fun getProducts(pgId: Int): ProductsResult {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"

    val ret = when (val csvData = readCsv(fileName)) {
        is CsvDataNotFound -> ProductsNotFound
        is CsvDataFound -> {
            val products = arrayListOf<Product>()
            csvData.data.forEach {
                products.add(Product(it[1].toInt(), it[0].toInt(), it[2], it[3].toDouble(),
                        it[4], it[5].toInt(), it[6], it[7] ))
            }
            ProductsFound(products)
        }
    }
    logger.debug(L_EXIT)
    return ret
}


fun getProduct(pgId: Int, pId: Int): ProductResult {
    logger.debug(L_ENTER)
    val ret = when (val products = getProducts(pgId)) {
        is ProductsNotFound -> ProductNotFound
        is ProductsFound -> {
            when (val p = products.data.firstOrNull { it.pId == pId }) {
                null -> ProductNotFound
                p -> ProductFound(p)
                else -> ProductNotFound
            }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

