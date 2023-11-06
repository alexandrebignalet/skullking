package org.skull.king.core.domain.state

import org.skull.king.application.infrastructure.framework.ddd.AggregateRoot
import org.skull.king.core.domain.*

sealed class Skullking(private val id: String) : AggregateRoot<String, SkullKingEvent> {
    companion object {

    }

    override fun getId(): String = id
    abstract val version: Int
    
    abstract override fun compose(e: SkullKingEvent, version: Int): Skullking

    open fun nextFirstPlayerIndex(): Int = 0

    protected fun distributeCards(
        gameId: String,
        players: List<String>,
        roundNb: Int,
        configuration: GameConfiguration
    ): List<NewPlayer> {
        val nextFirstPlayerIndex = nextFirstPlayerIndex()
        val deck = configuration.deck()
        val distributionOrder =
            players.subList(nextFirstPlayerIndex, players.size) + players.subList(0, nextFirstPlayerIndex)

        val cardsByPlayer: MutableMap<String, List<Card>> =
            distributionOrder.associateWith { listOf<Card>() }.toMutableMap()

        // distribute as many cards as possible with each player having the same number of cards
        // case: 9th/10th round at more than 6 players (BLACKROCK)
        val cardsToDistribute =
            if (roundNb * players.size > deck.size) deck.size / players.size
            else roundNb

        repeat((1..cardsToDistribute).count()) {
            distributionOrder.forEach { playerId ->
                cardsByPlayer[playerId]?.let { cards -> cardsByPlayer[playerId] = cards + deck.pop() }
            }
        }

        return distributionOrder.mapNotNull { playerId ->
            cardsByPlayer[playerId]?.let { cards -> NewPlayer(playerId, gameId, cards) }
        }
    }

    abstract fun playCard(playerId: String, card: Card): CardPlayed

    open fun settleFold(): Sequence<SkullKingEvent> {
        throw SkullKingNotReadyError("cannot settle a fold if game not in progress", this)
    }

    open fun start(gameId: String, playerIds: List<String>, configuration: GameConfiguration): Started {
        throw SkullKingError("SkullKing game already existing!", this)
    }
}

