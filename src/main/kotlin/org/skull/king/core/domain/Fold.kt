package org.skull.king.core.domain

import org.skull.king.core.domain.state.CardPlayed


data class PlayerCard(val playerId: PlayerId, val card: Card)
data class FoldSettlement(
    val nextFoldFirstPlayerId: PlayerId,
    val potentialBonus: Int = 0,
    val won: Boolean = true,
    val butinAllies: List<String> = listOf()
)

enum class BonusType {
    MERMAID_OVER_SKULLKING,
    SKULLKING_PER_PIRATE,
    PIRATE_PER_MERMAID,
    FOURTEEN_BLACK,
    FOURTEEN_DEFAULT,
    BUTIN_WINNER,
}

data class Fold(val cardsByPlayer: Map<PlayerId, Card> = mapOf()) {

    private val values = cardsByPlayer.map { PlayerCard(it.key, it.value) }

    val colorAsked
        get() = values.map(PlayerCard::card).filterIsInstance<ColoredCard>().firstOrNull()?.color

    val size
        get() = cardsByPlayer.size

    fun settle(configuration: GameConfiguration): FoldSettlement {
        val kraken = kraken()
        val whiteWhale = whiteWhale()
        val skullKing = skullKing()
        val pirates = pirates()
        val mermaids = mermaids()
        val highestBlackCard = highestOf(CardColor.BLACK)

        val butinsAllies = butins().map { it.playerId }
        val butinWinnerBonus =
            if (butins().isNotEmpty()) bonusResolver(
                configuration,
                BonusType.BUTIN_WINNER
            ) else 0
        val fourteensBonuses =
            (fourteenBlack()?.let { bonusResolver(configuration, BonusType.FOURTEEN_BLACK) }
                ?: 0) +
                    bonusResolver(
                        configuration,
                        BonusType.FOURTEEN_DEFAULT,
                        fourteensNotBlack().size
                    )

        return when {
            kraken.isNotEmpty() && whiteWhale.isNotEmpty() -> {
                val krakenOrWhiteWhaleFirst =
                    values.first { listOf(CardType.KRAKEN, CardType.WHITE_WHALE).contains(it.card.type) }
                val isWhiteWhaleFirst = krakenOrWhiteWhaleFirst.card.type == CardType.WHITE_WHALE

                return if (isWhiteWhaleFirst) krakenSettlement(configuration, true)
                else whiteWhaleSettlement()
            }

            kraken.isNotEmpty() -> krakenSettlement(configuration)

            whiteWhale.isNotEmpty() -> whiteWhaleSettlement()

            skullKing.isNotEmpty() -> when {
                mermaids.isNotEmpty() -> FoldSettlement(
                    nextFoldFirstPlayerId = mermaids.first().playerId,
                    potentialBonus = bonusResolver(
                        configuration,
                        BonusType.MERMAID_OVER_SKULLKING
                    ) + fourteensBonuses + butinWinnerBonus,
                    butinAllies = butinsAllies
                )

                else -> FoldSettlement(
                    nextFoldFirstPlayerId = skullKing.first().playerId,
                    potentialBonus = bonusResolver(
                        configuration,
                        BonusType.SKULLKING_PER_PIRATE,
                        pirates.size
                    ) + fourteensBonuses + butinWinnerBonus,
                    butinAllies = butinsAllies
                )
            }

            pirates.isNotEmpty() -> FoldSettlement(
                nextFoldFirstPlayerId = pirates.first().playerId,
                potentialBonus =
                bonusResolver(
                    configuration,
                    BonusType.PIRATE_PER_MERMAID,
                    mermaids.size
                ) + fourteensBonuses + butinWinnerBonus,
                butinAllies = butinsAllies
            )

            mermaids.isNotEmpty() -> FoldSettlement(
                mermaids.first().playerId,
                fourteensBonuses + butinWinnerBonus,
                butinAllies = butinsAllies
            )

            highestBlackCard != null -> FoldSettlement(
                highestBlackCard.playerId,
                fourteensBonuses + butinWinnerBonus,
                butinAllies = butinsAllies
            )

            onlyEscapes() -> FoldSettlement(values.first().playerId, 0, false)
            onlyEscapesAndButins() -> {
                val butins = values.filter { it.card.type == CardType.BUTIN }
                FoldSettlement(butins.first().playerId, 0, true)
            }

            else -> FoldSettlement(
                highestOf(colorAsked()!!)!!.playerId,
                fourteensBonuses + butinWinnerBonus,
                butinAllies = butinsAllies
            )
        }
    }

    private fun whiteWhaleSettlement(): FoldSettlement {
        val highestValueColored = highestOfColored()

        return highestValueColored
            ?.let { FoldSettlement(it.playerId, 0, true) }
            ?: FoldSettlement(values.first().playerId, 0, false)
    }

    private fun krakenSettlement(
        configuration: GameConfiguration,
        excludingWhiteWhale: Boolean = false
    ): FoldSettlement {
        val excludedCardsTypes =
            if (excludingWhiteWhale) listOf(CardType.WHITE_WHALE, CardType.KRAKEN) else listOf(CardType.KRAKEN)

        val foldWithoutKraken = values
            .filter { !excludedCardsTypes.contains(it.card.type) }
            .associate { it.playerId to it.card }

        return Fold(foldWithoutKraken)
            .settle(configuration)
            .copy(won = false, potentialBonus = 0)
    }

    private fun kraken() =
        values.filter { it.card.type == CardType.KRAKEN }

    private fun whiteWhale() =
        values.filter { it.card.type == CardType.WHITE_WHALE }

    private fun butins() =
        values.filter { it.card.type == CardType.BUTIN }

    private fun skullKing() =
        values.filter { it.card.type == CardType.SKULLKING }

    private fun mermaids() =
        values.filter { it.card.type == CardType.MERMAID }

    private fun pirates() = values.filter {
        it.card.type == CardType.PIRATE || (it.card is ScaryMary && it.card.usage == ScaryMaryUsage.PIRATE)
    }

    private fun onlyEscapes() = values.all { onlyEscapePredicate(it) }

    private fun onlyEscapePredicate(it: PlayerCard) =
        (it.card.type == CardType.ESCAPE) || (it.card is ScaryMary && it.card.usage == ScaryMaryUsage.ESCAPE)

    private fun onlyEscapesAndButins() = values.all {
        onlyEscapePredicate(it) || it.card.type == CardType.BUTIN
    }

    private fun fourteensNotBlack() =
        values.filter { it.card is ColoredCard && it.card.color != CardColor.BLACK && it.card.value == 14 }

    private fun fourteenBlack() =
        values.find { it.card is ColoredCard && it.card.color == CardColor.BLACK && it.card.value == 14 }

    private fun highestOf(colorAsked: CardColor) =
        values.filter { it.card is ColoredCard && it.card.color == colorAsked }
            .maxByOrNull { (it.card as ColoredCard).value }

    private fun highestOfColored() =
        values.filter { it.card is ColoredCard }.maxByOrNull { (it.card as ColoredCard).value }

    fun colorAsked() =
        values.firstOrNull { it.card is ColoredCard }?.let { (_, card) -> (card as ColoredCard).color }

    private fun bonusResolver(configuration: GameConfiguration, bonusType: BonusType, occurrences: Int = 1) =
        when (configuration) {
            is ClassicConfiguration -> when (bonusType) {
                BonusType.MERMAID_OVER_SKULLKING -> 50
                BonusType.SKULLKING_PER_PIRATE -> 30 * occurrences
                else -> 0
            }

            is BlackRockConfiguration -> when (bonusType) {
                BonusType.MERMAID_OVER_SKULLKING -> 40
                BonusType.SKULLKING_PER_PIRATE -> 30 * occurrences
                BonusType.PIRATE_PER_MERMAID -> 20 * occurrences
                BonusType.FOURTEEN_BLACK -> 20
                BonusType.FOURTEEN_DEFAULT -> 10 * occurrences
                BonusType.BUTIN_WINNER -> 20
            }
        }

    fun receive(event: CardPlayed) = copy(
        cardsByPlayer = cardsByPlayer.plus(Pair(event.playerId, event.card))
    )

    fun cards() = values.map { it.card }
    fun isEmpty() = size == 0
}