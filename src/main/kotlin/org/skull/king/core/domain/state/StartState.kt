package org.skull.king.core.domain.state

import org.skull.king.core.domain.Card
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.domain.SkullKingConfigurationError
import org.skull.king.core.domain.SkullKingNotStartedError

object StartState : Skullking("") {
    override val version: Int
        get() = 0

    override fun compose(e: SkullKingEvent, version: Int): Skullking = when (e) {
        is Started -> AnnounceState(
            e.gameId,
            e.players,
            e.configuration.firstRoundNb(),
            e.configuration,
            version
        )

        else -> this
    }

    override fun playCard(playerId: String, card: Card): CardPlayed {
        throw SkullKingNotStartedError(this)
    }

    override fun start(gameId: String, playerIds: List<String>, configuration: GameConfiguration): Started {
        val minimumPlayers = configuration.minimumPlayers()
        val maximumPlayers = configuration.maximumPlayers()
        if (playerIds.size !in (minimumPlayers..maximumPlayers)) {
            throw SkullKingConfigurationError(minimumPlayers, maximumPlayers, this)
        }
        return Started(
            gameId,
            distributeCards(gameId, playerIds.shuffled(), configuration.firstRoundNb(), configuration),
            configuration
        )
    }
}