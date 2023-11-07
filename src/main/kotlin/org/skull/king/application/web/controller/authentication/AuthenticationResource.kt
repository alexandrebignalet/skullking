package org.skull.king.application.web.controller.authentication

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.AUTH_COOKIE_NAME
import org.skull.king.application.infrastructure.authentication.User
import java.net.URI


@PermitAll
@Path("/skullking")
@Produces(MediaType.APPLICATION_JSON)
class AuthenticationResource {

    @Context
    lateinit var identity: SecurityContext


    @Inject
    lateinit var idGenerator: IdGenerator

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun register(): TemplateInstance
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun register(@FormParam("userName") userName: String): Response {
        val user = User(idGenerator.userId(), userName)
        return redirectToGameRooms(user)
    }


    @GET
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
    fun register(): Response =
        if (identity.userPrincipal == null) Response.ok(Templates.register()).build()
        else redirectToGameRooms(identity.userPrincipal as User)

    private fun redirectToGameRooms(user: User) = Response
        .status(302)
        .location(URI("/skullking/game_rooms"))
        .header("Set-Cookie", NewCookie.Builder(AUTH_COOKIE_NAME).value(user.name).build())
        .build()
}
