package org.skull.king.bot.domain.strategy

import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.RoundState

class Dumb : BotStrategy {

    override fun name(): String {
        return onePieceCharNames().random() + " Bot"
    }

    override fun computeCardPlay(skullKing: RoundState, currentPlayer: ReadyPlayer): Card {
        val colorAsked = skullKing.currentFold.colorAsked

        val hasColorAsked = currentPlayer.cards.any { it is ColoredCard && it.color == colorAsked }
        return currentPlayer.cards
            .filter {
                !hasColorAsked || it !is ColoredCard || it.color == colorAsked
            }
            .random()
            .let {
                if (it !is ScaryMary) it
                else if (Math.random() > 0.5) it.copy(usage = ScaryMaryUsage.ESCAPE)
                else it.copy(usage = ScaryMaryUsage.PIRATE)
            }
    }

    override fun computeAnnounce(skullKing: AnnounceState, currentPlayer: Player) = 1
}

private fun onePieceCharNames() = listOf(
    "Monkey D. Luffy",
    "Mother Carmel",
    "Hajrudin",
    "Streusen",
    "Sanji",
    "Makino",
    "Tsuru",
    "Conis",
    "Sabo",
    "Iceburg",
    "Cavendish",
    "Suleiman",
    "Dagama",
    "Orlumbus",
    "Sora",
    "Pudding",
    "Chiffon",
    "Sanji",
    "Charlotte Oven",
    "Mystoms",
    "Dr. Hiriluk",
    "Doctor",
    "Koshiro",
    "Streusen",
    "Oars: Extra",
    "Bartolomeo",
    "Monkey D. Luffy",
    "Nico Robin",
    "Usopp",
    "Brook",
    "Sanji",
    "Nami",
    "Roronoa Zoro",
    "Franky",
    "Shirahoshi: Extra",
    "Donquixote Doflamingo",
    "Rosinante",
    "St. Donquixote Homing",
    "Charlotte Perospero",
    "Monkey D. Luffy",
    "Roronoa Zoro",
    "Sanji",
    "Nami",
    "Usopp",
    "Nico Robin",
    "Franky",
    "Brook",
    "Ann",
    "Donald Moderate",
    "Emporio Ivankov",
    "Jinbe",
    "Buggy",
    "Marco",
    "Marco",
    "Cat Viper",
    "Charlotte Galette",
    "Charlotte Poire",
    "Montd'Or",
    "Wanda: Kingsbird",
    "Otsuru",
    "Kin'emon",
    "Charlotte Brulee",
    "Carrot",
    "Straw Hat Pirates",
    "Buena Festa",
    "Jigoro of the Wind &amp; Inuppe",
    "Bartolomeo",
    "Diamante",
    "Charlotte Daifuku",
    "Caesar Clown",
    "Shutenmaru",
    "Gecko Moria",
    "Nico Robin",
    "Tony Tony Chopper",
    "Franky",
    "Sanji",
    "Condoriano",
    "Sengoku",
    "Dracule Mihawk",
    "Makino",
    "Portgas D. Ace",
    "Sabo",
    "Otoko",
    "Whitey Bay",
    "Nefeltari Vivi"
)