package simpleserver.webserver

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.ktor.util.KtorExperimentalAPI
import simpleserver.util.L_ENTER
import simpleserver.util.L_EXIT
import simpleserver.util.getIntProperty
import java.util.*


private val mySessions = Collections.synchronizedSet(HashSet<String>())
private val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

sealed class ValidatedJwtResult
data class ValidatedJwtFound(val data: String) : ValidatedJwtResult()
data class ValidatedJwtNotFound(val msg: String) : ValidatedJwtResult()

@KtorExperimentalAPI
fun createJsonWebToken(email: String): String {
    logger.debug(L_ENTER)
    val expSecs = getIntProperty("jwt.json-web-token-expiration-as-seconds")
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, expSecs)
    val expirationDate = calendar.getTime()
    val jws = Jwts.builder()
        .setSubject(email)
        .signWith(key)
        .setExpiration(expirationDate)
        .compact()
    logger.trace("jsonWebToken: $jws")
    mySessions.add(jws)  // Side effect.
    logger.debug(L_EXIT)
    return jws
}

fun validateJsonWebToken(jwt: String): ValidatedJwtResult {
    logger.debug(L_ENTER)
    // Validata #1.
    val found = mySessions.contains(jwt)
    val ret = if (!found) {
        val msg = "Token not found in my sessions: $jwt"
        logger.warn(msg)
        ValidatedJwtNotFound(msg)
    } else {
        try {
            ValidatedJwtFound(Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody().getSubject())
        } catch (expiredEx: ExpiredJwtException) {
            val msg = "Token is expired, removing it from my sessions and returning nil: ${expiredEx.message}"
            logger.warn(msg)
            mySessions.remove(jwt) // Side effect.
            ValidatedJwtNotFound(msg)
        } catch (otherEx: JwtException) {
            val msg = "Some error in session handling: ${otherEx.message}"
            logger.error(msg)
            ValidatedJwtNotFound(msg)
        }
    }
    logger.debug(L_EXIT)
    return ret
}
