package org.skull.king.application.web.controller.authentication

import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.AUTH_COOKIE_NAME
import org.skull.king.application.infrastructure.authentication.User


@PermitAll
@Path("/skullking")
@Produces(MediaType.APPLICATION_JSON)
class AuthenticationResource {

    @Inject
    lateinit var idGenerator: IdGenerator

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    fun register(request: RegisterRequest): Response {
        val user = User(idGenerator.userId(), request.userName)
        return Response.ok()
            .header("Set-Cookie", NewCookie.Builder(AUTH_COOKIE_NAME).value(user.name).build())
            .build()
    }

    data class RegisterRequest(val userName: String)
}
