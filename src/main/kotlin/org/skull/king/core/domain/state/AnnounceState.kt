package org.skull.king.core.domain.state

import org.skull.king.core.domain.*

data class AnnounceState(
    val gameId: String,
    val players: List<Player>,
    val roundNb: Int,
    val configuration: GameConfiguration,
    override val version: Int
) : Skullking(gameId) {

    @Suppress("UNCHECKED_CAST")
    override fun compose(e: SkullKingEvent, version: Int) = when (e) {
        is PlayerAnnounced -> {
            val updatedPlayers = players.map {
                if (it.id == e.playerId) ReadyPlayer(it.id, gameId, it.cards, e.announce)
                else it
            }

            val allPlayersAnnounced = updatedPlayers.all { it is ReadyPlayer }
            if (!allPlayersAnnounced) {
                AnnounceState(gameId, updatedPlayers, roundNb, configuration, version)
            } else {
                RoundState(
                    gameId,
                    updatedPlayers.filterIsInstance<ReadyPlayer>(),
                    roundNb,
                    firstPlayerId = players.first().id,
                    configuration = configuration,
                    version = version
                )
            }
        }

        else -> this
    }

    override fun playCard(playerId: String, card: Card): CardPlayed {
        throw SkullKingNotReadyError(
            "All players must announce before starting to play cards",
            this
        )
    }

    fun hasAlreadyAnnounced(playerId: String) = players.any {
        it.id == playerId && it is ReadyPlayer
    }

    private fun has(playerId: String) = players.any { it.id == playerId }

    private fun isMissingOneLastAnnounce(): Boolean {
        return players.filterIsInstance<ReadyPlayer>().count() == players.count() - 1
    }

    fun announce(playerId: String, count: Int): Pair<String, Sequence<PlayerAnnounced>> {
        if (count < 0 || count > 10) {
            throw IllegalAnnounceError()
        }

        return when {
            hasAlreadyAnnounced(playerId) -> throw PlayerAlreadyAnnouncedError("Player $playerId already announced")

            has(playerId) -> Pair(
                gameId,
                sequenceOf(
                    PlayerAnnounced(
                        gameId,
                        playerId,
                        count,
                        roundNb,
                        isMissingOneLastAnnounce(),
                        version
                    )
                )
            )

            else -> throw PlayerNotInGameError(playerId, this)
        }
    }
}