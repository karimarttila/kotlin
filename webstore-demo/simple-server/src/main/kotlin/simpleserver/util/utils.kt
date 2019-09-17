package simpleserver.util

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

const val packageName = "util"
val logger: Logger = LoggerFactory.getLogger(packageName)

sealed class CsvData
data class CsvDataFound(val data: List<Array<String>>) : CsvData()
object CsvDataNotFound : CsvData()


public fun readCsv(fileName: String): CsvData {
    logger.debug(L_ENTER)
    val url = ClassLoader.getSystemClassLoader().getResource(fileName) ?: return CsvDataNotFound
    val filePath = File(url.file).toPath().toString()
    val parser = CSVParserBuilder().withSeparator('\t').build()
    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
    val ret = csvReader.use { input -> input.readAll() }
    logger.debug(L_EXIT)
    return CsvDataFound(ret)
}