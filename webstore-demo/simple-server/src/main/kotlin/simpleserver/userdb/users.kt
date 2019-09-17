package simpleserver.userdb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.*

const val packageName = "userdb"
val logger: Logger = LoggerFactory.getLogger(packageName)

private val users = getInitialUsers()

private fun getInitialUsers(): HashMap<String, Array<String>> {
    logger.debug(L_ENTER)
    val fileName = "users.csv"
    val ret = HashMap<String, Array<String>>()
    when (val csvData = readCsv(fileName)) {
        is CsvDataNotFound -> throw Exception("Could not find initial set of users")
        is CsvDataFound -> {
            csvData.data.forEach { ret[it[0]] = arrayOf(it[1], it[2], it[3], it[4]) }
        }
    }
    logger.debug(L_EXIT)
    return ret
}

@Synchronized fun getUsers(): Map<String, Array<String>> { return HashMap(users) }

// TODO: CONTINUE HERE:
//fun emailAlreadyExists
//fun addUser
//fun checkCredentials