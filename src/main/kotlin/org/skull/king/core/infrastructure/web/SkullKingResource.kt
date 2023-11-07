package org.skull.king.core.infrastructure.web

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import jakarta.annotation.security.PermitAll
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.application.web.redirectToGame
import org.skull.king.application.web.redirectToGameRoom
import org.skull.king.core.domain.Card
import org.skull.king.core.domain.ScaryMaryUsage
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCountSaga
import org.skull.king.core.usecases.GetGame
import org.skull.king.core.usecases.PlayCardSaga
import org.skull.king.core.usecases.captor.ReadPlayer
import org.skull.king.core.usecases.captor.ReadSkullKing
import org.skull.king.game_room.domain.GameRoom
import org.skull.king.game_room.infrastructure.GameRoomService


@PermitAll
@Path("/skullking/games")
@Produces(MediaType.APPLICATION_JSON)
class SkullKingResource(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus,
    private val gameRoomService: GameRoomService,
    private val context: SecurityContext,
) {

    @CheckedTemplate
    object Templates {
        @JvmStatic
        external fun game(
            skullKing: ReadSkullKing,
            user: User,
            gameRoom: GameRoom,
            currentPlayer: ReadPlayer
        ): TemplateInstance
    }

    @GET
    @Path("/{game_id}")
    @Produces(MediaType.TEXT_HTML)
    fun game(@PathParam("game_id") gameId: String): Response = queryBus.send(GetGame(gameId)).let {
        val gameRoom = gameRoomService.findOneBy(gameId)
        val user = context.userPrincipal
        if (it == null || gameRoom == null || user == null) {
            var error = ""
            if (it == null) error = error.plus("Game not available;")
            if (gameRoom == null) error = error.plus("GameRoom not available;")
            if (user == null) error = error.plus("Unknown user")
            return redirectToGameRoom(error)
        }

        val principal = user as User
        val currentPlayer = it.players.first { it.id == principal.id }
        Response.ok(Templates.game(it, principal, gameRoom, currentPlayer)).build()
    }

    @POST
    @Path("/{game_id}/players/{player_id}/announce")
    @Consumes(MediaType.APPLICATION_JSON)
    fun announce(
        @PathParam("game_id") gameId: String,
        @PathParam("player_id") playerId: String,
        @Valid request: AnnounceWinningCardsFoldCountRequest
    ): Response {
        val command = AnnounceWinningCardsFoldCountSaga(gameId, playerId, request.count)
        commandBus.send(command)
        return Response.noContent().build()
    }

    @POST
    @Path("/{game_id}/players/{player_id}/announce")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun announceHtml(
        @PathParam("game_id") gameId: String,
        @PathParam("player_id") playerId: String,
        @FormParam("announce") announce: Int,
    ): Response {
        val command = AnnounceWinningCardsFoldCountSaga(gameId, playerId, announce)
        commandBus.send(command)
        return redirectToGame(gameId)
    }

    @POST
    @Path("/{game_id}/players/{player_id}/play")
    @Consumes(MediaType.APPLICATION_JSON)
    fun play(
        @PathParam("game_id") gameId: String,
        @PathParam("player_id") playerId: String,
        @Valid request: PlayCardRequest
    ): Response {

        val command = PlayCardSaga(gameId, playerId, request.card)

        commandBus.send(command)

        return Response.noContent().build()
    }

    @POST
    @Path("/{game_id}/players/{player_id}/play")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun play(
        @PathParam("game_id") gameId: String,
        @PathParam("player_id") playerId: String,
        @FormParam("cardId") cardId: String,
        @FormParam("usage") usage: String?,
    ): Response {

        val scaryMaryUsage = usage?.let { ScaryMaryUsage.valueOf(it) } ?: ScaryMaryUsage.NOT_SET
        val command = PlayCardSaga(gameId, playerId, Card.fromId(cardId, scaryMaryUsage))

        commandBus.send(command)

        return redirectToGame(gameId)
    }

    data class AnnounceWinningCardsFoldCountRequest(val count: Int)

    class PlayCardRequest(val card: Card)

}
