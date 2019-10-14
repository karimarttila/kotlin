package simpleserver.userdb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT


const val packageName = "domainTest"
val logger: Logger = LoggerFactory.getLogger(packageName)

class DomainTest {

    @Test
    fun getUsersTest() {
        logger.debug(L_ENTER)
        val testUser = User("timo.tillinen@foo.com", "Timo", "Tillinen", "EE5F0C6F4D191B58497F7DB5C5C9CAF8")
        val initialUsers = getUsers()
        assertEquals(3, initialUsers.size)
        val timoUser: User? = initialUsers["2"]
        assertTrue(timoUser == testUser)
        logger.debug(L_EXIT)
    }

    @Test
    fun emailAlreadyExistsTest() {
        logger.debug(L_ENTER)
        val retOk = emailAlreadyExists("kari.karttinen@foo.com");
        val retNotOk = emailAlreadyExists("NOT.FOUND@foo.com");
        assertTrue(retOk);
        assertFalse(retNotOk);
        logger.debug(L_EXIT)
    }

}