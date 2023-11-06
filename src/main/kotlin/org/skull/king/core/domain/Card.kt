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
sealed class Card(val type: CardType) {
    open fun id(): String = type.name
}

data class ColoredCard(val value: Int, val color: CardColor) : Card(CardType.COLORED) {
    override fun id(): String = "${type}_${color}_$value"
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
    override fun id(): String = "${type}_$name"
}

object SkullkingCard : Card(CardType.SKULLKING)
data class Mermaid(val name: MermaidName = MermaidName.NONE) : Card(CardType.MERMAID) {
    override fun id(): String = "${type}_$name"
}

object Escape : Card(CardType.ESCAPE)
object Kraken : Card(CardType.KRAKEN)
object WhiteWhale : Card(CardType.WHITE_WHALE)
object Butin : Card(CardType.BUTIN)

enum class ScaryMaryUsage { ESCAPE, PIRATE, NOT_SET }
data class ScaryMary(val usage: ScaryMaryUsage = ScaryMaryUsage.NOT_SET) : Card(CardType.SCARY_MARY) {
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