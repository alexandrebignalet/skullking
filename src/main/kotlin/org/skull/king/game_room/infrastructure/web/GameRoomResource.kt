package org.skull.king.game_room.infrastructure.web

import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.bot.domain.strategy.BotStrategy.BotStrategyType
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


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): List<GameRoom> {
        val user: User = context.userPrincipal as User
        return service.findAll()
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
    @Path("/{game_room_id}/bots")
    @Consumes(MediaType.APPLICATION_JSON)
    fun addBot(
        @PathParam("game_room_id") gameRoomId: String,
        request: AddBotRequest,
    ): Response {
        service.join(gameRoomId, GameUser.bot(idGenerator.botId(), request.strategy))
        return Response.noContent().build()
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

    data class StartResponse(val gameId: String)
    data class AddBotRequest(val strategy: BotStrategyType = BotStrategyType.Dumbot)
}
