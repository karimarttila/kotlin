package simpleserver.domaindb

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import java.io.File


val packageName = "domaindb"
val logger: Logger = LoggerFactory.getLogger(packageName)
val infoMsg = "index.html => Info in HTML format";

fun getInfo(): String {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return infoMsg
}


@Override
fun getProductGroups(): Map<String, String> {
    logger.debug(L_ENTER)
    val rawData = readCsv("product-groups.csv")
    var ret = HashMap<String, String>()
    rawData.forEach { ret.put(it.get(0), it.get(1)) }
    logger.debug(L_EXIT)
    return ret
}

private fun readCsv(fileName: String): List<Array<String>> {
    logger.debug(L_ENTER)
    val filePath = File(ClassLoader.getSystemClassLoader().getResource(fileName).file).toPath().toString()
    val parser = CSVParserBuilder().withSeparator('\t').build()
    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
    val ret = csvReader.use { input -> input.readAll() }
    logger.debug(L_EXIT)
    return ret
}