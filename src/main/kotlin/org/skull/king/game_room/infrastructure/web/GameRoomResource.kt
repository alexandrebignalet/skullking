package org.skull.king.game_room.infrastructure.web

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.application.web.redirectToGame
import org.skull.king.application.web.redirectToGameRoom
import org.skull.king.game_room.domain.Configuration
import org.skull.king.game_room.domain.GameRoom
import org.skull.king.game_room.domain.GameUser
import org.skull.king.game_room.infrastructure.GameRoomService


@PermitAll
@Path("/skullking/game_rooms")
class GameRoomResource {
    @Inject
    lateinit var service: GameRoomService

    @Context
    lateinit var context: SecurityContext

    @Context
    lateinit var idGenerator: IdGenerator

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun gameRooms(rooms: List<GameRoom>, user: User, error: String?): TemplateInstance
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun index(@QueryParam("error") error: String?): TemplateInstance {
        val user: User = context.userPrincipal as User
        val rooms = service.findAll()
        return Templates.gameRooms(rooms, user, error)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createRoom(configuration: Configuration?): Response {
        val user: User = context.userPrincipal as User
        val gameRoomId = service.create(GameUser.from(user), configuration)
        return Response.ok(CreateGameRoomResponse(gameRoomId)).build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun createRoomTemplate(): Response {
        val user: User = context.userPrincipal as User
        service.create(GameUser.from(user), null)
        return redirectToGameRoom()
    }

    @POST
    @Path("/{game_room_id}/bots")
    fun addBot(
        @PathParam("game_room_id") gameRoomId: String,
        @Context request: ContainerRequestContext
    ): Response {
        runCatching {
            service.join(gameRoomId, GameUser.bot(idGenerator.botId()))
        }.onFailure {

        }

        return redirectToGameRoom()
    }

    @POST
    @Path("/{game_room_id}/users")
    fun join(
        @PathParam("game_room_id") gameRoomId: String,
    ): Response {
        val user: User = context.userPrincipal as User
        service.join(gameRoomId, GameUser.from(user))
        return Response.noContent().build()
    }

    @POST
    @Path("/{game_room_id}/launch")
    @Produces(MediaType.APPLICATION_JSON)
    fun launch(@PathParam("game_room_id") gameRoomId: String): Response {
        val user: User = context.userPrincipal as User
        val gameId = service.startGame(gameRoomId, user.id)
        return Response.ok(StartResponse(gameId)).build()
    }

    @POST
    @Path("/{game_room_id}/launch")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun launchHtml(@PathParam("game_room_id") gameRoomId: String): Response {
        val user: User = context.userPrincipal as User
        val gameId = service.startGame(gameRoomId, user.id)
        return redirectToGame(gameId)
    }

    data class StartResponse(val gameId: String)
}
