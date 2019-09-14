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

sealed class RawData
data class RawDataFound(val data: List<Array<String>>): RawData()
object RawDataNotFound: RawData()


fun getInfo(): String {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return infoMsg
}

fun getProductGroups(): Map<String, String> {
    logger.debug(L_ENTER)
    val rawData = readCsv("product-groups.csv")
    val ret = HashMap<String, String>()
    when(rawData) {
        is RawDataFound -> rawData.data.forEach { ret[it[0]] = it[1] }
    }
    logger.debug(L_EXIT)
    return ret
}

fun getProducts(pgId: Int): RawData {
    logger.debug(L_ENTER)
    val fileName = "pg-${pgId}-products.csv"
    val ret = readCsv(fileName)
    logger.debug(L_EXIT)
    return ret
}

// TODO: JATKA TÄSTÄ...
//fun getProduct(pgId: Int, pId: Int): List<Array<String>> {
//    logger.debug(L_ENTER)
//    val fileName = "pg-${pgId}-products.csv"
//    try {
//        val ret = readCsv(fileName)
//    }
//    logger.debug(L_EXIT)
//    return ret
//}


private fun readCsv(fileName: String): RawData {
    logger.debug(L_ENTER)
    val url = ClassLoader.getSystemClassLoader().getResource(fileName) ?: return RawDataNotFound
    val filePath = File(url.file).toPath().toString()
    val parser = CSVParserBuilder().withSeparator('\t').build()
    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
    val ret = csvReader.use { input -> input.readAll() }
    logger.debug(L_EXIT)
    return RawDataFound(ret)
}