package org.skull.king.bot.domain.strategy

import org.skull.king.core.domain.Card
import org.skull.king.core.domain.Player
import org.skull.king.core.domain.ReadyPlayer
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.RoundState

sealed interface BotStrategy {

    companion object {
        fun from(type: BotStrategyType): BotStrategy = when (type) {
            BotStrategyType.Dumbot -> Dumb()
            BotStrategyType.NasusBot -> Nasus()
        }
    }

    enum class BotStrategyType {
        Dumbot,
        NasusBot
    }

    fun name(): String
    fun computeAnnounce(skullKing: AnnounceState, currentPlayer: Player): Int
    fun computeCardPlay(skullKing: RoundState, currentPlayer: ReadyPlayer): Card


    fun winningOddsOf(cards: List<Card>, playersCount: Int, card: Card): Double {
        var universe = cards.count()
        var strongerCardsCount = cards.count { it > card }
        var cumulatedOdds = 1.0

        for (i in 1..playersCount) {
            cumulatedOdds *= (universe - strongerCardsCount) / universe.toDouble()
            strongerCardsCount--
            universe--
        }

        return cumulatedOdds
    }
}
