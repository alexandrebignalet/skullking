package org.skull.king.core.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
sealed interface Player {
    val id: String
    val gameId: String
    val cards: List<Card>
}

data class NewPlayer(override val id: String, override val gameId: String, override val cards: List<Card>) : Player

data class ReadyPlayer(
    override val id: String,
    override val gameId: String,
    override val cards: List<Card>,
    val announce: Int,
    val done: Int = 0,
) : Player

fun List<ReadyPlayer>.sortPlayersForNextRound(lastFoldWinnerPlayerId: PlayerId): List<ReadyPlayer> {
    val winnerIndex = this.map { it.id }.indexOf(lastFoldWinnerPlayerId)
    return this.subList(winnerIndex, this.size) + this.subList(0, winnerIndex)
}