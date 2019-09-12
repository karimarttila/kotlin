package simpleserver.domaindb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT

val packageName = "domaindb"

fun getInfo(): String {
    val logger: Logger = LoggerFactory.getLogger(packageName)
    logger.debug(L_ENTER)
    logger.debug(L_EXIT)
    return "TODO"
}

