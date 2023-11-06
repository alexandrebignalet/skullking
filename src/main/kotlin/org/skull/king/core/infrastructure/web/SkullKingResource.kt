package org.skull.king.core.infrastructure.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.annotation.security.PermitAll
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.core.domain.Card
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCountSaga
import org.skull.king.core.usecases.PlayCardSaga


@PermitAll
@Path("/skullking/games")
@Produces(MediaType.APPLICATION_JSON)
class SkullKingResource(private val commandBus: CommandBus){


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

    data class AnnounceWinningCardsFoldCountRequest(val count: Int)

    class PlayCardRequest(val card: Card)

}
