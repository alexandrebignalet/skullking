package org.skull.king.core.usecases.captor

import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.*

data class ReadSkullKing(
    val id: String,
    val players: List<ReadPlayer>,
    val roundNb: RoundNb,
    val fold: List<Play> = listOf(),
    val isEnded: Boolean = false,
    val phase: SkullKingPhase,
    val currentPlayerId: String,
    val scoreBoard: ScoreBoard = mapOf()
) {
    fun onPlayerAnnounced(event: PlayerAnnounced) = copy(
        phase = if (event.isLast) SkullKingPhase.CARDS else SkullKingPhase.ANNOUNCEMENT,
        scoreBoard = scoreBoard.entries.associate { (playerId, roundScores) ->
            val newRoundScores =
                if (event.playerId == playerId)
                    roundScores + RoundScore(announced = event.announce, roundNb = event.roundNb)
                else roundScores

            playerId to newRoundScores
        })

    fun onCardPlayed(event: CardPlayed) = ReadCard.from(event.card).let { card ->
        copy(
            currentPlayerId = nextPlayerAfter(currentPlayerId),
            players = players.map { player ->
                if (player.id == event.playerId) {
                    val cards = player.cards.toMutableList()
                    cards.remove(card)
                    player.copy(cards = cards)
                } else player
            },
            fold = fold + Play(event.playerId, card),
        )
    }

    private fun nextPlayerAfter(currentPlayerId: String): String {
        val playersIds = players.map { it.id }
        val currentPlayerIndex = playersIds.indexOf(currentPlayerId)
        return if (currentPlayerIndex == players.size - 1) playersIds.first()
        else playersIds[currentPlayerIndex + 1]
    }

    fun onFoldSettled(event: FoldSettled) = copy(
        fold = listOf(),
        currentPlayerId = event.winnerPlayerId,
        scoreBoard = scoreBoard.entries.associate { (playerId, roundScores) ->
            val newRoundScores = roundScores.mapIndexed { index, roundScore ->
                val roundNb = index + 1
                if (roundNb != this.roundNb) return@mapIndexed roundScore

                var potentialBonus = roundScore.potentialBonus
                val roundFoldsDone = roundScore.done + if (event.won) 1 else 0
                val roundCanStillBeSuccessful = roundScore.announced < roundFoldsDone
                if (event.butinAllies.contains(playerId) && roundCanStillBeSuccessful) {
                    potentialBonus += 20
                }

                if (event.won && event.winnerPlayerId == playerId) {
                    potentialBonus += event.bonus
                    roundScore.copy(done = roundFoldsDone, potentialBonus = potentialBonus)
                } else roundScore.copy(potentialBonus = potentialBonus)
            }
            playerId to newRoundScores
        }
    )

    fun onNewRoundStarted(event: NewRoundStarted) = copy(
        roundNb = event.roundNb,
        phase = SkullKingPhase.ANNOUNCEMENT,
        currentPlayerId = event.players.first().id,
        players = event.players.map { ReadPlayer(it.id, it.gameId, it.cards.map(ReadCard::from)) }
    )

    fun onGameFinished() = copy(isEnded = true)

    companion object {
        fun create(event: Started): ReadSkullKing {
            val firstPlayerId = event.players.first().id
            return ReadSkullKing(
                id = event.gameId,
                players = event.players.map {
                    ReadPlayer(
                        id = it.id,
                        gameId = event.gameId,
                        cards = it.cards.map(ReadCard::from)
                    )
                },
                roundNb = 1,
                phase = SkullKingPhase.ANNOUNCEMENT,
                currentPlayerId = firstPlayerId,
                scoreBoard = event.players.associate { it.id to listOf() }
            )
        }
    }

}

typealias ScoreBoard = Map<String, List<RoundScore>>

typealias RoundNb = Int

data class RoundScore(
    val announced: Int,
    val done: Int = 0,
    val potentialBonus: Int = 0,
    val roundNb: RoundNb
)

enum class SkullKingPhase {
    ANNOUNCEMENT, CARDS
}

data class ReadPlayer(
    val id: String,
    val gameId: String,
    val cards: List<ReadCard> = listOf()
)

data class Play(
    val playerId: String,
    val card: ReadCard
)

data class ReadCard(
    val type: String,
    val value: Int? = null,
    val color: String? = null,
    val usage: String? = null,
    val name: String? = null,
    val id: String? = null
) {
    companion object {
        fun from(card: Card) = when (card) {
            is ColoredCard -> ReadCard(
                type = card.type.name,
                value = card.value,
                color = card.color.name
            )

            is Mermaid -> ReadCard(type = card.type.name, name = card.name.name)
            is Pirate -> ReadCard(type = card.type.name, name = card.name.name)
            is ScaryMary -> ReadCard(type = card.type.name, usage = card.usage.name)

            Butin -> ReadCard(type = card.type.name)
            Escape -> ReadCard(type = card.type.name)
            Kraken -> ReadCard(type = card.type.name)
            SkullkingCard -> ReadCard(type = card.type.name)
            WhiteWhale -> ReadCard(type = card.type.name)
        }
    }
}

enum class ReadCardType { SKULLKING, ESCAPE, PIRATE, SCARY_MARY, COLORED, MERMAID }
