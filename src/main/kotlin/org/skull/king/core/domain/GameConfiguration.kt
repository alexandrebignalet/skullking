package org.skull.king.core.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.skull.king.game_room.domain.Configuration

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "variant",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ClassicConfiguration::class, name = GameConfiguration.CLASSIC),
    JsonSubTypes.Type(value = BlackRockConfiguration::class, name = GameConfiguration.BLACKROCK),
)
sealed class GameConfiguration(
    open protected val availableCards: List<Card>,
    open protected val deckShuffling: Boolean
) {
    companion object {
        const val CLASSIC = "CLASSIC"
        const val BLACKROCK = "BLACKROCK"

        fun from(configuration: Configuration?) =
            configuration?.let { BlackRockConfiguration.from(it) } ?: ClassicConfiguration()


        private const val MIN_PLAYERS = 2
        private const val MAX_PLAYERS = 6
        private const val FIRST_ROUND_NB = 1
        private const val MAX_ROUND = 10
    }

    fun minimumPlayers(): Int = MIN_PLAYERS
    fun maximumPlayers(): Int = MAX_PLAYERS
    fun firstRoundNb(): Int = FIRST_ROUND_NB
    fun maxRoundNb(): Int = MAX_ROUND

    protected abstract fun defaultCards(): List<Card>

    fun deck() = Deck(availableCards.ifEmpty { defaultCards() }, deckShuffling)
}

data class ClassicConfiguration(
    override val availableCards: List<Card> = listOf(),
    override val deckShuffling: Boolean = true
) :
    GameConfiguration(availableCards, deckShuffling) {

    override fun defaultCards() = listOf(
        *(1..13).map { ColoredCard(it, CardColor.YELLOW) }.toTypedArray(),
        *(1..13).map { ColoredCard(it, CardColor.RED) }.toTypedArray(),
        *(1..13).map { ColoredCard(it, CardColor.BLUE) }.toTypedArray(),
        *(1..13).map { ColoredCard(it, CardColor.BLACK) }.toTypedArray(),
        *(1..5).map { Escape }.toTypedArray(),
        *(1..2).map { Mermaid() }.toTypedArray(),
        Pirate(PirateName.EVIL_EMMY),
        Pirate(PirateName.HARRY_THE_GIANT),
        Pirate(PirateName.TORTUGA_JACK),
        Pirate(PirateName.BADEYE_JOE),
        Pirate(PirateName.BETTY_BRAVE),
        SkullkingCard,
        ScaryMary(ScaryMaryUsage.NOT_SET)
    )
}

data class BlackRockConfiguration(
    val kraken: Boolean,
    val whale: Boolean,
    val butins: Boolean,
    override val availableCards: List<Card> = listOf(),
    override val deckShuffling: Boolean = true
) : GameConfiguration(availableCards, deckShuffling) {

    companion object {

        fun from(gameRoomConfiguration: Configuration) = BlackRockConfiguration(
            kraken = gameRoomConfiguration.withKraken,
            whale = gameRoomConfiguration.withWhale,
            butins = gameRoomConfiguration.withButins,
        )
    }

    override fun defaultCards() = listOf(
        *(if (kraken) arrayOf(Kraken) else arrayOf()),
        *(if (whale) arrayOf(WhiteWhale) else arrayOf()),
        *(if (butins) arrayOf(Butin, Butin) else arrayOf()),
        *(1..14).map { ColoredCard(it, CardColor.YELLOW) }.toTypedArray(),
        *(1..14).map { ColoredCard(it, CardColor.GREEN) }.toTypedArray(),
        *(1..14).map { ColoredCard(it, CardColor.PURPLE) }.toTypedArray(),
        *(1..14).map { ColoredCard(it, CardColor.BLACK) }.toTypedArray(),
        *(1..5).map { Escape }.toTypedArray(),
        Mermaid(MermaidName.CIRCE),
        Mermaid(MermaidName.ALYRA),
        Pirate(PirateName.ROSIE_LA_DOUCE),
        Pirate(PirateName.WILL_LE_BANDIT),
        Pirate(PirateName.RASCAL_LE_FLAMBEUR),
        Pirate(PirateName.JUANITA_JADE),
        Pirate(PirateName.HARRY_LE_GEANT),
        SkullkingCard,
        ScaryMary(ScaryMaryUsage.NOT_SET)
    )
}
