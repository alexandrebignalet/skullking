package org.skull.king.core.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

enum class CardType {
    ESCAPE,
    MERMAID,
    COLORED,
    PIRATE,
    SCARY_MARY,
    SKULLKING,
    KRAKEN,
    WHITE_WHALE,
    BUTIN
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ColoredCard::class, name = "COLORED"),
    JsonSubTypes.Type(value = SkullkingCard::class, name = "SKULLKING"),
    JsonSubTypes.Type(value = ScaryMary::class, name = "SCARY_MARY"),
    JsonSubTypes.Type(value = Pirate::class, name = "PIRATE"),
    JsonSubTypes.Type(value = Escape::class, name = "ESCAPE"),
    JsonSubTypes.Type(value = Mermaid::class, name = "MERMAID"),
    JsonSubTypes.Type(value = Butin::class, name = "BUTIN"),
    JsonSubTypes.Type(value = Kraken::class, name = "KRAKEN"),
    JsonSubTypes.Type(value = WhiteWhale::class, name = "WHITE_WHALE"),
)
sealed class Card(val type: CardType) : Comparable<Card> {
    abstract override operator fun compareTo(other: Card): Int

    open val id get(): String = type.name

    companion object {
        fun fromId(cardId: String, usage: ScaryMaryUsage): Card {
            val split = cardId.split("__")
            val type = CardType.valueOf(split.first())
            return when (type) {
                CardType.ESCAPE -> Escape
                CardType.MERMAID -> Mermaid(MermaidName.valueOf(split[1]))
                CardType.COLORED -> ColoredCard(split[2].toInt(), CardColor.valueOf(split[1]))
                CardType.PIRATE -> Pirate(PirateName.valueOf(split[1]))
                CardType.SCARY_MARY -> ScaryMary(usage)
                CardType.SKULLKING -> SkullkingCard
                CardType.KRAKEN -> Kraken
                CardType.WHITE_WHALE -> WhiteWhale
                CardType.BUTIN -> Butin
            }
        }
    }
}

data class ColoredCard(val value: Int, val color: CardColor) : Card(CardType.COLORED) {
    override fun compareTo(other: Card): Int {
        return when (other) {
            Butin -> 1
            is ColoredCard -> when (other.color) {
                color -> value.compareTo(other.value)
                CardColor.BLACK -> -1
                else -> 1
            }

            Escape -> 1
            Kraken -> -1
            is Mermaid -> -1
            is Pirate -> -1
            is ScaryMary -> if (other.usage == ScaryMaryUsage.ESCAPE) 1 else -1
            SkullkingCard -> -1
            WhiteWhale -> -1
        }
    }

    override val id get(): String = "${type}__${color}__$value"
}

enum class CardColor { RED, BLUE, YELLOW, BLACK, GREEN, PURPLE }

enum class PirateName {
    HARRY_THE_GIANT,
    TORTUGA_JACK,
    EVIL_EMMY,
    BADEYE_JOE,
    BETTY_BRAVE,

    ROSIE_LA_DOUCE,
    WILL_LE_BANDIT,
    RASCAL_LE_FLAMBEUR,
    JUANITA_JADE,
    HARRY_LE_GEANT
}

enum class MermaidName {
    NONE,
    CIRCE,
    ALYRA
}

data class Pirate(val name: PirateName) : Card(CardType.PIRATE) {
    override fun compareTo(other: Card): Int {
        return when (other) {
            Butin -> 1
            is ColoredCard -> 1
            Escape -> 1
            Kraken -> -1
            is Mermaid -> 1
            is Pirate -> 1
            is ScaryMary -> 1
            SkullkingCard -> -1
            WhiteWhale -> -1
        }
    }

    override val id get(): String = "${type}__$name"
}

object SkullkingCard : Card(CardType.SKULLKING) {
    override fun compareTo(other: Card): Int {
        return when (other) {
            Butin -> 1
            is ColoredCard -> 1
            Escape -> 1
            Kraken -> -1
            is Mermaid -> -1
            is Pirate -> 1
            is ScaryMary -> 1
            SkullkingCard -> 1
            WhiteWhale -> -1
        }
    }
}

data class Mermaid(val name: MermaidName = MermaidName.NONE) : Card(CardType.MERMAID) {
    override fun compareTo(other: Card): Int {
        return when (other) {
            Butin -> 1
            is ColoredCard -> 1
            Escape -> 1
            Kraken -> -1
            is Mermaid -> 1
            is Pirate -> -1
            is ScaryMary -> if (other.usage == ScaryMaryUsage.ESCAPE) 1 else -1
            SkullkingCard -> 1
            WhiteWhale -> -1
        }
    }

    override val id get(): String = "${type}__$name"
}

object Escape : Card(CardType.ESCAPE) {
    override fun compareTo(other: Card): Int = -1
}

object Kraken : Card(CardType.KRAKEN) {
    override fun compareTo(other: Card): Int = -1
}

object WhiteWhale : Card(CardType.WHITE_WHALE) {
    override fun compareTo(other: Card): Int = -1
}

object Butin : Card(CardType.BUTIN) {
    override fun compareTo(other: Card): Int = if (other is Butin) 1 else -1
}

enum class ScaryMaryUsage { ESCAPE, PIRATE, NOT_SET }
data class ScaryMary(val usage: ScaryMaryUsage = ScaryMaryUsage.NOT_SET) : Card(CardType.SCARY_MARY) {
    override fun compareTo(other: Card): Int {
        return when (other) {
            Butin -> 1
            is ColoredCard -> 1
            Escape -> 1
            Kraken -> -1
            is Mermaid -> 1
            is Pirate -> 1
            is ScaryMary -> 1
            SkullkingCard -> -1
            WhiteWhale -> -1
        }
    }

    override fun equals(other: Any?) = other is ScaryMary
    override fun hashCode() = 1
}

data class Deck(val cards: List<Card>, val shuffled: Boolean = true) {
    private val deck = cards
        .let {
            if (shuffled) it.shuffled()
            else it
        }
        .fold(Stack<Card>()) { acc, s -> acc.push(s); acc }

    val size: Int get() = cards.size
    fun pop(): Card = deck.pop()
}
