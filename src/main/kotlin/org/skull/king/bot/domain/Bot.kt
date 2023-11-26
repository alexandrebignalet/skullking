package org.skull.king.bot.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.RoundState
import org.skull.king.core.domain.state.SkullKingEvent
import org.skull.king.core.domain.state.Skullking
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCountSaga
import org.skull.king.core.usecases.PlayCardSaga
import org.skull.king.game_room.domain.GameRoom
import org.skull.king.game_room.domain.GameUser.BotGameUser

data class Bot(
    val trigger: SkullKingEvent,
    val gameUser: BotGameUser,
    val state: State = Void()
) {

    companion object {
        fun from(room: GameRoom, event: SkullKingEvent) = room.bots.map { Bot(event, it) }
    }

    fun withContext(context: Skullking) = when (context) {
        is AnnounceState -> Bot(trigger, gameUser, Announce(context))
        is RoundState -> Bot(trigger, gameUser, PlayCard(context))
        else -> Bot(trigger, gameUser, Void(context))
    }

    fun command() = state.command(trigger, gameUser)
}

sealed interface State {
    fun command(trigger: SkullKingEvent, gameUser: BotGameUser): BotActionResult<Command<*>>
}

data class Announce(val game: AnnounceState) : State {

    override fun command(
        trigger: SkullKingEvent,
        gameUser: BotGameUser
    ): BotActionResult<AnnounceWinningCardsFoldCountSaga> {
        if (game.hasAlreadyAnnounced(gameUser.id)) {
            return BotActionResult.failure("${gameUser.name} has already played on ${trigger.javaClass.simpleName} for ${game::class.simpleName}:${game.version}")
        }

        val currentPlayer = game.players.find { it.id == gameUser.id }

        return BotActionResult.success(
            AnnounceWinningCardsFoldCountSaga(
                gameId = game.gameId,
                playerId = gameUser.id,
                count = if (currentPlayer == null) 1
                else gameUser.strategy.computeAnnounce(game, currentPlayer)
            )
        )
    }
}

data class PlayCard(val game: RoundState) : State {
    override fun command(trigger: SkullKingEvent, gameUser: BotGameUser): BotActionResult<PlayCardSaga> {
        val currentPlayer = game.currentPlayer()
        if (currentPlayer?.id != gameUser.id) {
            val objectMapper = ObjectMapper()
            val debugMessage =
                if (currentPlayer != null) "" else " : current is null -> ${objectMapper.writeValueAsString(game)}"
            return BotActionResult.failure("it is not ${gameUser.name} turn to play on ${trigger.javaClass.simpleName} for ${game.javaClass.simpleName}:${game.version} $debugMessage")
        }

        return BotActionResult.success(
            PlayCardSaga(
                gameId = game.gameId,
                playerId = gameUser.id,
                card = gameUser.strategy.computeCardPlay(game, currentPlayer)
            )
        )
    }
}

data class Void(val game: Skullking? = null) : State {
    override fun command(trigger: SkullKingEvent, gameUser: BotGameUser) =
        BotActionResult.failure<Command<*>>("${gameUser.name} has nothing to do on ${trigger::class.simpleName} for ${game?.javaClass?.simpleName}:${game?.version}")
}
