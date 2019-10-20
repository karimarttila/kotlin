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
data class NewUser(val data: User) : UserResult()
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

/**
 * Makes a copy of users for the client.
 * @return copy of users
 */
fun getUsers(): Map<String, User> {
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return HashMap(users)
}

/**
 * Checks if the email already exists in the database.
 * @param email to check
 * @return true if email already exists, false otherwise
 */
fun emailAlreadyExists(givenEmail: String): Boolean {
    logger.debug(L_ENTER)
    val user = users.elements().toList().firstOrNull { it.email == givenEmail}
    val ret = user != null
    logger.debug(L_EXIT)
    return ret
}

// Probably not necessary to synchronize since both users and counter are already synchronized.
/**
 * Adds a new user.
 * First checks that the email is not already in use. If not in use, adds the new user to the database.
 * @return If email already exists in the db, returns UserAddError, otherwise NewUser.
 */
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

/**
 * Checks if credentials exist in the database.
 * @return true if credentials found, false otherwise.
 * @throws Exception if something went wrong (to satisfy when else clause).
 */
fun checkCredentials(email: String, password: String): Boolean {
    logger.debug(L_ENTER)
    val ret = when (val user = (users.toList().firstOrNull { it.second.email == email })?.second) {
        null -> false
        is User -> user.password == DigestUtils.md5Hex(password).toUpperCase()
        // Should not be here.
        else -> throw Exception("Something went wrong while checking credentials with user email: " + email)
    }
    logger.debug(L_EXIT)
    return ret
}
