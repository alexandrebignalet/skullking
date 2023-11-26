package org.skull.king.core.domain.state

import org.skull.king.core.domain.*

data class RoundState(
    val gameId: String,
    val players: List<ReadyPlayer>,
    val roundNb: Int,
    val currentFold: Fold = Fold(),
    val discarded: List<Fold> = listOf(),
    val foldPlayedNb: Int = 0,
    val firstPlayerId: String,
    val configuration: GameConfiguration,
    override val version: Int
) : Skullking(gameId) {

    override fun playCard(playerId: String, card: Card) = when {
        !has(playerId) -> throw PlayerNotInGameError(playerId, this)
        !isPlayerTurn(playerId) -> throw NotYourTurnError(playerId, this)
        doesPlayerHaveCard(playerId, card) -> when {
            card is ScaryMary && card.usage == ScaryMaryUsage.NOT_SET -> throw ScaryMaryUsageError(this)
            isCardPlayNotAllowed(playerId, card) -> throw CardNotAllowedError(card, this)
            else -> CardPlayed(gameId, playerId, card, isLastFoldPlay(), version)
        }

        else -> throw PlayerDoNotHaveCardError(playerId, card, this)
    }

    override fun settleFold(): Sequence<SkullKingEvent> {
        if (!isFoldComplete()) {
            throw FoldNotComplete(this)
        }

        val (nextFoldFirstPlayer, potentialBonus, won, butinAllies) = currentFold.settle(configuration)
        val events = sequenceOf(FoldSettled(gameId, nextFoldFirstPlayer, potentialBonus, won, butinAllies, version))

        if (!isNextFoldLastFoldOfRound()) {
            return events
        }

        if (isOver()) {
            return events + GameFinished(gameId, version)
        }

        val newRoundNb = roundNb + 1
        return events + RoundFinished(
            getId(),
            newRoundNb,
            distributeCards(
                this.gameId,
                players.map { it.id },
                newRoundNb,
                configuration
            ),
            version
        )
    }

    override fun compose(e: SkullKingEvent, version: Int) = when (e) {
        is CardPlayed -> RoundState(
            gameId = gameId,
            players = removeCardFromPlayerHand(e),
            roundNb = roundNb,
            currentFold = currentFold.receive(e),
            discarded = discarded,
            foldPlayedNb = foldPlayedNb,
            firstPlayerId = firstPlayerId,
            configuration = configuration,
            version = version
        )

        is FoldSettled -> RoundState(
            gameId = gameId,
            players = players
                .map {
                    if (it.id == e.winnerPlayerId && e.won) it.copy(done = it.done + 1)
                    else it
                }
                .sortPlayersForNextRound(e.winnerPlayerId),
            roundNb = roundNb,
            foldPlayedNb = foldPlayedNb + 1,
            firstPlayerId = firstPlayerId,
            configuration = configuration,
            discarded = discarded + currentFold,
            version = version
        )

        is RoundFinished -> AnnounceState(
            gameId = gameId,
            players = e.players,
            roundNb = roundNb + 1,
            configuration = configuration,
            version = version
        )

        is GameFinished -> OverState
        else -> this
    }

    override fun nextFirstPlayerIndex(): Int = players.map { it.id }
        .indexOf(firstPlayerId)
        .let { if (it == players.size - 1) 0 else it + 1 }

    private fun doesPlayerHaveCard(playerId: String, card: Card): Boolean =
        players.find { it.id == playerId }?.cards?.any { it == card } ?: false

    private fun has(playerId: String) = players.any { it.id == playerId }

    private fun isFoldComplete() = players.size == currentFold.size
    private fun isLastFoldPlay() = players.size - 1 == currentFold.size

    private fun isCardPlayNotAllowed(playerId: PlayerId, card: Card) = players.find { it.id == playerId }?.let {
        !CardService.isCardPlayAllowed(currentFold.cards(), it.cards, card)
    } ?: false

    private fun isNextFoldLastFoldOfRound() = foldPlayedNb + 1 == roundNb

    private fun isOver() = roundNb + 1 > configuration.maxRoundNb()

    private fun isPlayerTurn(playerId: PlayerId): Boolean {
        val firstDidNotPlay: PlayerId? = players.firstOrNull { it.cards.size == (roundNb - foldPlayedNb) }?.id
        return firstDidNotPlay?.let { it == playerId } ?: false
    }

    private fun removeCardFromPlayerHand(event: CardPlayed) = players.map {
        if (it.id == event.playerId) {
            val mutableList = it.cards.toMutableList()
            mutableList.remove(event.card)
            ReadyPlayer(it.id, it.gameId, mutableList.toList(), it.announce)
        } else it
    }

    fun currentPlayer(): ReadyPlayer? {
        return players.firstOrNull() { isPlayerTurn(it.id) }
    }
}
