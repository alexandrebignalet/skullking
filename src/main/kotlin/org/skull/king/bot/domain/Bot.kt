package org.skull.king.bot.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.RoundState
import org.skull.king.core.domain.state.SkullKingEvent
import org.skull.king.core.domain.state.Skullking
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCountSaga
import org.skull.king.core.usecases.PlayCardSaga
import org.skull.king.game_room.domain.GameRoom
import org.skull.king.game_room.domain.GameUser

data class Bot(val trigger: SkullKingEvent, val gameUser: GameUser, val state: State = Void()) {

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
    fun command(trigger: SkullKingEvent, gameUser: GameUser): BotActionResult<Command<*>>
}

data class Announce(val game: AnnounceState) : State {

    override fun command(
        trigger: SkullKingEvent,
        gameUser: GameUser
    ): BotActionResult<AnnounceWinningCardsFoldCountSaga> {
        if (game.hasAlreadyAnnounced(gameUser.id)) {
            return BotActionResult.failure("${gameUser.name} has already played on ${trigger.javaClass.simpleName} for ${game::class.simpleName}:${game.version}")
        }

        return BotActionResult.success(
            AnnounceWinningCardsFoldCountSaga(
                gameId = game.gameId,
                playerId = gameUser.id,
                count = computeAnnounce()
            )
        )
    }

    private fun computeAnnounce() = 1
}

data class PlayCard(val game: RoundState) : State {
    override fun command(trigger: SkullKingEvent, gameUser: GameUser): BotActionResult<PlayCardSaga> {
        val currentPlayer = game.currentPlayer()
        if (currentPlayer?.id != gameUser.id) {
            val objectMapper = ObjectMapper()
            val debugMessage =
                if (currentPlayer != null) "" else " : current is null -> ${objectMapper.writeValueAsString(game)}"
            return BotActionResult.failure("it is not ${gameUser.name} turn to play on ${trigger.javaClass.simpleName} for ${game.javaClass.simpleName}:${game.version} $debugMessage")
        }

        val card = computeCardToPlay(currentPlayer)
            .let { if (it is ScaryMary) it.copy(usage = chooseScaryMaryUsage()) else it }

        return BotActionResult.success(
            PlayCardSaga(
                gameId = game.gameId,
                playerId = gameUser.id,
                card = card
            )
        )
    }

    private fun computeCardToPlay(currentPlayer: ReadyPlayer): Card {
        val colorAsked = game.currentFold.colorAsked
        val hasColorAsked = currentPlayer.cards.any { it is ColoredCard && it.color == colorAsked }
        if (colorAsked == null || !hasColorAsked) {
            return currentPlayer.cards.random()
        }

        return currentPlayer.cards
            .filter { it !is ColoredCard || it.color == colorAsked }
            .random()
    }

    private fun chooseScaryMaryUsage() = if (Math.random() > 0.5) ScaryMaryUsage.ESCAPE else ScaryMaryUsage.PIRATE
}

data class Void(val game: Skullking? = null) : State {
    override fun command(trigger: SkullKingEvent, gameUser: GameUser) =
        BotActionResult.failure<Command<*>>("${gameUser.name} has nothing to do on ${trigger::class.simpleName} for ${game?.javaClass?.simpleName}:${game?.version}")
}

sealed interface BotActionResult<out T> {

    companion object {
        fun <T> success(result: T) = Success(result)
        fun <T> failure(failure: String) = Failure<T>(failure)
    }

    fun onFailure(block: (failure: String) -> Unit): BotActionResult<T> {
        if (this is Failure) {
            block(this.failure)
        }
        return this
    }

    fun onSuccess(block: (success: T) -> Unit): BotActionResult<T> {
        if (this is Success) {
            block(this.result)
        }
        return this
    }

    data class Success<T>(val result: T) : BotActionResult<T>
    data class Failure<T>(val failure: String) : BotActionResult<T>

}