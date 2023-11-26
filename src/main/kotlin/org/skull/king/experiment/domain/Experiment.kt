package org.skull.king.experiment.domain

import org.skull.king.bot.domain.strategy.BotStrategy.BotStrategyType
import java.time.Instant

data class Experiment(
    val id: String,
    val playersCount: Int,
    val botRepartition: Map<BotStrategyType, Int>,
    val skullKingId: String? = null,
    val createdAt: Instant = Instant.now()
) {

    init {
        require(botRepartition.values.sum() == playersCount) { "Bot repartition must sum to playersCount ($playersCount)" }
    }

    fun allocateBotStrategies() = BotStrategyType.entries
        .map { type ->
            val count = botRepartition.getOrDefault(type, 0)
            (1..count).map { type }
        }
        .flatten()
}
