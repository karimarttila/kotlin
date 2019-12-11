package simpleserver.userdb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT


const val packageName = "domainTest"
val logger: Logger = LoggerFactory.getLogger(packageName)

class DomainTest {

    @BeforeEach
    fun setup() {
        initializeUserDb()
    }

    @Test
    fun getUsersTest() {
        logger.debug(L_ENTER)
        val testUser = User("timo.tillinen@foo.com", "Timo", "Tillinen", "EE5F0C6F4D191B58497F7DB5C5C9CAF8")
        val initialUsers = getUsers()
        val timoUser: User? = initialUsers["2"]
        assertTrue(timoUser == testUser)
        logger.debug(L_EXIT)
    }

    @Test
    fun emailAlreadyExistsTest() {
        logger.debug(L_ENTER)
        val retOk = emailAlreadyExists("kari.karttinen@foo.com")
        val retNotOk = emailAlreadyExists("NOT.FOUND@foo.com")
        assertTrue(retOk)
        assertFalse(retNotOk)
        logger.debug(L_EXIT)
    }

    @Test
    fun addUserTest() {
        logger.debug(L_ENTER)
        val initialUsers = getUsers()
        val initialUsersSize = initialUsers.size
        // Adding new user with non-conflicting email.
        when (val newUser = addUser("jamppa.jamppanen@foo.com", "Jamppa", "Jamppanen", "JampanSalasana")) {
            is NewUser -> {
                val currentUsers = getUsers()
                assertEquals(initialUsersSize + 1, currentUsers.size)
                assertEquals("jamppa.jamppanen@foo.com", newUser.data.email)
                assertEquals("Jamppa", newUser.data.firstName)
                assertEquals("Jamppanen", newUser.data.LastName)
            }
            else -> assertTrue(false, "Didn't return NewUser")
        }
        // Trying to add the same email again.
        when (val failedUser = addUser("jamppa.jamppanen@foo.com", "Jamppa", "Jamppanen", "JampanSalasana")) {
            is UserAddError -> {
                val currentUsers = getUsers()
                assertEquals(initialUsersSize + 1, currentUsers.size)
                assertEquals("Email already exists: " + "jamppa.jamppanen@foo.com", failedUser.msg)
            }
            else -> assertTrue(false, "Didn't return UserAddError")
        }

        logger.debug(L_EXIT)
    }

    @Test
    fun checkCredentialsTest() {
        logger.debug(L_ENTER)
        val retOk = checkCredentials("kari.karttinen@foo.com", "Kari")
        val retFailedPassword = checkCredentials("kari.karttinen@foo.com", "FAILED-PASSWORD")
        val retEmailNotFound = checkCredentials("NOT.FOUND@foo.com", "Kari")
        assertTrue(retOk)
        assertFalse(retFailedPassword)
        assertFalse(retEmailNotFound)
        logger.debug(L_EXIT)
    }

}