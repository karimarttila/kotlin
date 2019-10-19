package simpleserver.userdb

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.*
import java.util.concurrent.ConcurrentHashMap

const val packageName = "userdb"
val logger: Logger = LoggerFactory.getLogger(packageName)

data class User(val email: String, val firstName: String, val LastName: String, val password: String)

sealed class UserResult
data class UserFound(val data: User) : UserResult()
data class NewUser(val data: User) : UserResult()
object UserNotFound : UserResult()
data class UserAddError(val msg: String) : UserResult()


/** Proxy for performance reasons. */
private val users = getInitialUsers()
private var counter = users.size

private fun getInitialUsers(): ConcurrentHashMap<String, User> {
    logger.debug(L_ENTER)
    val fileName = "users.csv"

    val ret = when (val csvData = readCsv(fileName)) {
        // Consider as unrecoverable error.
        is CsvDataNotFound -> throw Exception("Could not find initial set of users")
        is CsvDataFound -> {
            val users = ConcurrentHashMap<String, User>()
            csvData.data.forEach { users[it[0]] = User(it[1], it[2], it[3], it[4]) }
            users
        }
    }
    logger.debug(L_EXIT)
    return ret
}

@Synchronized private fun nextCounter(): Int {
    counter++
    return counter
}

fun getUsers(): Map<String, User> {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return HashMap(users)
}

fun emailAlreadyExists(givenEmail: String): Boolean {
    logger.debug(L_ENTER)
    val user = users.elements().toList().firstOrNull { it.email == givenEmail}
    val ret = user != null
    logger.debug(L_EXIT)
    return ret
}

@Synchronized fun addUser(email: String, firstName: String, lastName: String, password: String): UserResult {
    logger.debug(L_ENTER)
    val ret : UserResult = when(emailAlreadyExists(email)) {
        true -> UserAddError("Email already exists: " + email)
        else -> {
            val newUser = User(email, firstName, lastName, DigestUtils.md5Hex(password).toUpperCase())
            // Side effect: add new user to database.
            users.put(nextCounter().toString(), newUser)
            NewUser(newUser)
        }
    }
    logger.debug(L_EXIT)
    return ret
}

// TODO: CONTINUE HERE:
//fun addUser
//fun checkCredentials