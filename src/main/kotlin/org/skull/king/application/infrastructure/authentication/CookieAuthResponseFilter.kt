package org.skull.king.application.infrastructure.authentication

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.ext.Provider
import java.security.Principal

@Provider
class CookieAuthResponseFilter : ContainerResponseFilter {

    override fun filter(request: ContainerRequestContext, response: ContainerResponseContext) {
        val principal: Principal? = request.securityContext.userPrincipal
        if (request.securityContext !is CookieSecurityContext) {
            return
        }

        if (principal is User) {
            response.headers.add("Set-Cookie", NewCookie.Builder(AUTH_COOKIE_NAME).value(principal.name).build())
        } else if (request.cookies.containsKey(AUTH_COOKIE_NAME)) {
            //the principal has been unset during the response, delete the cookie
            response.headers.remove("Set-Cookie")
        }
    }
}

