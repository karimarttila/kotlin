package simpleserver.domaindb

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import java.io.File

const val packageName = "domaindb"
val logger: Logger = LoggerFactory.getLogger(packageName)
const val infoMsg = "index.html => Info in HTML format"

sealed class ProductGroups
data class ProductGroupsFound(val data: List<Array<String>>) : ProductGroups()
object ProductGroupsNotFound : ProductGroups()

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
        is ProductGroupsFound -> pgData.data.forEach { ret[it[0]] = it[1] }
    }
    logger.debug(L_EXIT)
    return ret
}

fun getProducts(pgId: Int): ProductGroups {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"
    val ret = readCsv(fileName)
    logger.debug(L_EXIT)
    return ret
}

fun getProduct(pgId: Int, pId: Int): Product {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"
    val ret = when (val pgData = readCsv(fileName)) {
        is ProductGroupsNotFound -> ProductNotFound
        is ProductGroupsFound ->
            when (val p = pgData.data.firstOrNull { it[0].equals(pId.toString()) }) {
                null -> ProductNotFound
                p -> ProductFound(arrayOf(p[0], p[1], p[2], p[3]))
                else -> ProductNotFound
            }
    }
    logger.debug(L_EXIT)
    return ret
}

private fun readCsv(fileName: String): ProductGroups {
    logger.debug(L_ENTER)
    val url = ClassLoader.getSystemClassLoader().getResource(fileName) ?: return ProductGroupsNotFound
    val filePath = File(url.file).toPath().toString()
    val parser = CSVParserBuilder().withSeparator('\t').build()
    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
    val ret = csvReader.use { input -> input.readAll() }
    logger.debug(L_EXIT)
    return ProductGroupsFound(ret)
}