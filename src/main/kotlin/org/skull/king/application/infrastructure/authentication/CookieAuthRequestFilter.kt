package org.skull.king.application.infrastructure.authentication


import jakarta.annotation.Priority
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.Cookie
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger


data class CookieSecurityContext(val user: User) : SecurityContext {
    override fun getUserPrincipal() = user
    override fun isUserInRole(role: String) = true
    override fun isSecure() = false
    override fun getAuthenticationScheme() = "COOKIE"
}

const val AUTH_COOKIE_NAME: String = "skullking-auth"

@Priority(Priorities.AUTHENTICATION)
@Provider
@PreMatching
class CookieAuthRequestFilter : ContainerRequestFilter {
    
    override fun filter(crc: ContainerRequestContext) {
        crc.cookies[AUTH_COOKIE_NAME]
            ?.let(Cookie::getValue)
            ?.takeIf { it.isNotEmpty() }
            ?.let { cookieValue ->
                runCatching {
                    crc.securityContext = CookieSecurityContext(CookieCredentials(cookieValue).toUser())
                }
                    .onSuccess {
                        return
                    }
                    .onFailure {
                        throw InternalServerErrorException(it)
                    }
            }
    }
}

