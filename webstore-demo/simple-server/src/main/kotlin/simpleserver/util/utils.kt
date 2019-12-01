package simpleserver.util

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding
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

var ssEnv: String = System.getenv("SS_ENV") ?: "dev"
val config = systemProperties() overriding
             EnvironmentVariables() overriding
             ConfigurationProperties.fromResource("application-${ssEnv}.conf")

/**
 * Reads csv.
 * If data found in proxy, returns that data.
 * If data not found in proxy, reads data from file, adds data to proxy and returns data.
 *
 * @param fileName Filename to read (and used as key in proxy).
 * @return CsvData entity
 */
fun readCsv(fileName: String): CsvData {
    logger.debug(L_ENTER)
    val ret = when (val csvData = csvFiles[fileName]) {
        is CsvDataNotFound -> csvData
        is CsvDataFound -> csvData
        // Didn't find it in the proxy, let's read it from file.
        null -> {
            val fromFile = when (val url = ClassLoader.getSystemClassLoader().getResource(fileName)) {
                null -> CsvDataNotFound
                else -> {
                    // NOTE: In real production code we should handle bad data here.
                    val filePath = File(url.file).toPath().toString()
                    val parser = CSVParserBuilder().withSeparator('\t').build()
                    val csvReader = CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
                    CsvDataFound(csvReader.use { input -> input.readAll() })
                }

            }
            // This filename read, add to proxy.
            csvFiles[fileName] = fromFile
            fromFile
        }
    }
    logger.debug(L_EXIT)
    return ret
}

