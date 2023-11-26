package org.skull.king.bot.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.approvaltests.Approvals
import org.junit.jupiter.api.Test
import org.skull.king.core.domain.Card
import org.skull.king.core.domain.ClassicConfiguration


class BotStrategyTest {


    @Test
    fun `should calculate each card winning odds against number of player`() {

        val result = (2..6).associateWith { playersCount ->
            val byCard = ClassicConfiguration().deck().cards.associate { card ->
                val odds = winningOddsOf(playersCount, card)

                card.id to "${Math.round(odds * 100)}%"
            }

            byCard
        }

        val jsonResult = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result)
        Approvals.verify(jsonResult)
    }

    fun winningOddsOf(playersCount: Int, card: Card): Double {
        val cards = ClassicConfiguration().deck().cards

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