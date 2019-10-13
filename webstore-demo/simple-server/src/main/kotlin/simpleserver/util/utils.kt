package simpleserver.util

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

const val packageName = "util"
val logger: Logger = LoggerFactory.getLogger(packageName)

sealed class CsvData
data class CsvDataFound(val data: List<Array<String>>) : CsvData()
object CsvDataNotFound : CsvData()

/** Proxy for performance reasons. */
val csvFiles = ConcurrentHashMap<String, CsvData>()

/**
 * Reads csv.
 * If data found in proxy, returns that data.
 * If data not found in proxy, reads data from file, adds data to proxy and returns data.
 *
 * @param fileName Filename to read (and used as key in proxy).
 * @return CsvData entity
 */
public fun readCsv(fileName: String): CsvData {
    logger.debug(L_ENTER)
    val csvData = csvFiles.get(fileName)
    val ret = when (csvData) {
        is CsvDataNotFound -> csvData
        is CsvDataFound -> csvData
        // Didn't find it in the proxy, let's read it from file.
        null -> {
            val url = ClassLoader.getSystemClassLoader().getResource(fileName)
            val fromFile = when (url) {
                null -> CsvDataNotFound
                else -> {
                    val filePath = File(url.file).toPath().toString()
                    val parser = CSVParserBuilder().withSeparator('\t').build()
                    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
                    CsvDataFound(csvReader.use { input -> input.readAll() })
                }

            }
            // This filename read, add to proxy.
            csvFiles.put(fileName, fromFile)
            fromFile
        }
    }
    logger.debug(L_EXIT)
    return ret
}

