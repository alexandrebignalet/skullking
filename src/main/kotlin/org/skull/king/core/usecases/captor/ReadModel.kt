package org.skull.king.core.usecases.captor

import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.*
import org.skull.king.core.usecases.captor.SkullKingPhase.ANNOUNCEMENT
import org.skull.king.game_room.domain.GameRoom
import kotlin.math.abs

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
        phase = if (event.isLast) SkullKingPhase.CARDS else ANNOUNCEMENT,
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
                    val cardIds = player.cards.map { it.id }.toMutableList()
                    cardIds.remove(card.id)
                    player.copy(cards = cardIds.map { id -> player.cards.first { id == it.id } })
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

    fun onNewRoundStarted(event: RoundFinished) = copy(
        roundNb = event.roundNb,
        phase = ANNOUNCEMENT,
        currentPlayerId = event.players.first().id,
        players = event.players.map { new ->
            ReadPlayer(
                id = new.id,
                gameId = new.gameId,
                cards = new.cards.map(ReadCard::from),
                name = players.find { it.id == new.id }?.name ?: ""
            )
        }
    )

    fun onGameFinished() = copy(isEnded = true)

    companion object {
        fun create(event: Started, gameRoom: GameRoom?): ReadSkullKing {
            val firstPlayerId = event.players.first().id
            return ReadSkullKing(
                id = event.gameId,
                players = event.players.map {
                    ReadPlayer(
                        id = it.id,
                        name = gameRoom?.userNameOf(it.id) ?: "",
                        gameId = event.gameId,
                        cards = it.cards.map(ReadCard::from)
                    )
                },
                roundNb = 1,
                phase = ANNOUNCEMENT,
                currentPlayerId = firstPlayerId,
                scoreBoard = event.players.associate { it.id to listOf() }
            )
        }
    }

    fun currentRoundScoreOf(playerId: String): RoundScore? = scoreBoard[playerId]?.find { it.roundNb == roundNb }
    fun isAnnouncePhase() = phase == ANNOUNCEMENT
    fun isCardsPhase() = phase == SkullKingPhase.CARDS
    fun subscribeUrl() = "$id/subscribe"
    fun scoreOf(playerId: String) = scoreBoard[playerId]
        ?.filter { !isEnded || roundNb == it.roundNb }
        ?.sumOf(RoundScore::score)
}

typealias ScoreBoard = Map<String, List<RoundScore>>

typealias RoundNb = Int

data class RoundScore(
    val announced: Int,
    val done: Int = 0,
    val potentialBonus: Int = 0,
    val roundNb: RoundNb
) {
    val score
        get() = when {
            announced == done -> when {
                announced == 0 -> 10 * roundNb
                else -> announced * 20
            } + potentialBonus

            else -> when {
                announced == 0 -> 10 * roundNb * -1
                else -> abs(announced - done) * 10 * -1
            }
        }
}

enum class SkullKingPhase {
    ANNOUNCEMENT, CARDS
}

data class ReadPlayer(
    val id: String,
    val gameId: String,
    val cards: List<ReadCard> = listOf(),
    val name: String
) {

    fun playCardUrl(card: ReadCard) = "$gameId/players/$id/play"
    fun announceUrl() = "$gameId/players/$id/announce"
    fun announceAvailable(): List<Int> = (0..cards.size).toList()
}

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
    val id: String
) {
    companion object {
        fun from(card: Card) = when (card) {
            is ColoredCard -> ReadCard(
                type = card.type.name,
                value = card.value,
                color = card.color.name,
                id = card.id
            )

            is Mermaid -> ReadCard(type = card.type.name, name = card.name.name, id = card.id)
            is Pirate -> ReadCard(type = card.type.name, name = card.name.name, id = card.id)
            is ScaryMary -> ReadCard(type = card.type.name, usage = card.usage.name, id = card.id)

            Butin -> ReadCard(type = card.type.name, id = card.id)
            Escape -> ReadCard(type = card.type.name, id = card.id)
            Kraken -> ReadCard(type = card.type.name, id = card.id)
            SkullkingCard -> ReadCard(type = card.type.name, id = card.id)
            WhiteWhale -> ReadCard(type = card.type.name, id = card.id)
        }
    }

    fun isScaryMary() = this.type == CardType.SCARY_MARY.name
}

enum class ReadCardType { SKULLKING, ESCAPE, PIRATE, SCARY_MARY, COLORED, MERMAID }
