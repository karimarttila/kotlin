package simpleserver.util

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
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

@KtorExperimentalAPI
val config = HoconApplicationConfig(ConfigFactory.load())

@KtorExperimentalAPI
fun getIntProperty(key: String): Int {
    val value = (config.propertyOrNull(key))?.getString()
    if (value == null) throw IllegalStateException("Couldn't find property with key: $key")
    return try {
        value.toInt()
    } catch (e: NumberFormatException) {
        throw IllegalStateException("The value for key: $key was not numeric: $value")
    }
}

fun getStringPropertyOrNull(key: String): String? {
    return (config.propertyOrNull(key))?.getString()
}

class SSResource {}

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
    val resourceDir = getStringPropertyOrNull("misc.resourcedir")
    if (resourceDir == null)
        logger.debug("Didn't find misc.resourcedir key from configuration, using classloader path")
    else
        logger.debug("Using resource path (from misc.resourcedir configuration): $resourceDir")
    val ret = when (val csvData = csvFiles[fileName]) {
        is CsvDataNotFound -> csvData
        is CsvDataFound -> csvData
        // Didn't find it in the proxy, let's read it from file.
        null -> {
            val url = if (resourceDir == null)
                SSResource::class.java.classLoader.getResource(fileName)
            else
                File("${resourceDir}/${fileName}").toURI().toURL()
            logger.debug("url: $url")

            val fromFile = when (url) {
                null -> CsvDataNotFound
                else -> {
                    // NOTE: In real production code we should handle bad data here.
                    val filePath = File(url.file).toPath().toString()
                    val parser = CSVParserBuilder().withSeparator('\t').build()
                    val csvReader =
                        CSVReaderBuilder(File(filePath).inputStream().bufferedReader()).withCSVParser(parser).build()
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

