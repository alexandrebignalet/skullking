package org.skull.king.bot.domain.strategy

import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.RoundState

class Nasus : BotStrategy {

    companion object {
        private const val ODDS_THRESHOLD_FOR_ANNOUNCE = 0.5
    }

    override fun name(): String {
        return leagueOfLegendsChampNames().random() + " Bot"
    }

    override fun computeCardPlay(skullKing: RoundState, currentPlayer: ReadyPlayer) =
        compute(skullKing, currentPlayer).let {
            if (it !is ScaryMary) it
            else it.copy(usage = ScaryMaryUsage.PIRATE)
        }

    private fun compute(
        skullKing: RoundState,
        currentPlayer: ReadyPlayer
    ): Card {
        val colorAsked = skullKing.currentFold.colorAsked

        val hasColorAsked = currentPlayer.cards.any { it is ColoredCard && it.color == colorAsked }
        val validCards = currentPlayer.cards.filter {
            !hasColorAsked || it !is ColoredCard || it.color == colorAsked
        }

        val remainingCards =
            skullKing.configuration.deck().cards.filter { card -> card !in skullKing.discarded.flatMap { it.cards() } }

        val sortedByPower = validCards.sortedBy {
            winningOddsOf(remainingCards, skullKing.players.count(), it)
        }

        val announced = currentPlayer.announce
        val done = currentPlayer.done
        val remainingFoldsToWin = announced - done

        if (skullKing.currentFold.isEmpty()) {
            return sortedByPower.first()
        }

        if (remainingFoldsToWin > 0) {
            return sortedByPower.last()
        }

        return sortedByPower.first()
    }

    override fun computeAnnounce(skullKing: AnnounceState, currentPlayer: Player) = currentPlayer.cards
        .map { card ->
            winningOddsOf(
                skullKing.configuration.deck().cards,
                skullKing.players.count(),
                card
            )
        }
        .count { it >= ODDS_THRESHOLD_FOR_ANNOUNCE }

}

private fun leagueOfLegendsChampNames() = listOf(
    "Aatrox",
    "Ahri",
    "Akali",
    "Akshan",
    "Alistar",
    "Amumu",
    "Anivia",
    "Annie",
    "Aphelios",
    "Ashe",
    "Aurelion Sol",
    "Azir",
    "Bard",
    "Bel'Veth",
    "Blitzcrank",
    "Brand",
    "Braum",
    "Caitlyn",
    "Camille",
    "Cassiopeia",
    "Cho'Gath",
    "Corki",
    "Darius",
    "Diana",
    "Dr. Mundo",
    "Draven",
    "Ekko",
    "Elise",
    "Evelynn",
    "Ezreal",
    "Fiddlesticks",
    "Fiora",
    "Fizz",
    "Galio",
    "Gangplank",
    "Garen",
    "Gnar",
    "Gragas",
    "Graves",
    "Gwen",
    "Hecarim",
    "Heimerdinger",
    "Illaoi",
    "Irelia",
    "Ivern",
    "Janna",
    "Jarvan IV",
    "Jax",
    "Jayce",
    "Jhin",
    "Jinx",
    "K'Sante",
    "Kai'Sa",
    "Kalista",
    "Karma",
    "Karthus",
    "Kassadin",
    "Katarina",
    "Kayle",
    "Kayn",
    "Kennen",
    "Kha'Zix",
    "Kindred",
    "Kled",
    "Kog'Maw",
    "LeBlanc",
    "Lee Sin",
    "Leona",
    "Lillia",
    "Lissandra",
    "Lucian",
    "Lulu",
    "Lux",
    "Malphite",
    "Malzahar",
    "Maokai",
    "Master Yi",
    "Miss Fortune",
    "Mordekaiser",
    "Morgana",
    "Nami",
    "Nasus",
    "Nautilus",
    "Neeko",
    "Nidalee",
    "Nilah",
    "Nocturne",
    "Nunu & Willump",
    "Olaf",
    "Orianna",
    "Ornn",
    "Pantheon",
    "Poppy",
    "Pyke",
    "Qiyana",
    "Quinn",
    "Rakan",
    "Rammus",
    "Rek'Sai",
    "Rell",
    "Renata Glasc",
    "Renekton",
    "Rengar",
    "Riven",
    "Rumble",
    "Ryze",
    "Samira",
    "Sejuani",
    "Senna",
    "Seraphine",
    "Sett",
    "Shaco",
    "Shen",
    "Shyvana",
    "Singed",
    "Sion",
    "Sivir",
    "Skarner",
    "Sona",
    "Soraka",
    "Swain",
    "Sylas",
    "Syndra",
    "Tahm Kench",
    "Taliyah",
    "Talon",
    "Taric",
    "Teemo",
    "Thresh",
    "Tristana",
    "Trundle",
    "Tryndamere",
    "Twisted Fate",
    "Twitch",
    "Udyr",
    "Urgot",
    "Varus",
    "Vayne",
    "Veigar",
    "Vel'Koz",
    "Vex",
    "Vi",
    "Viego",
    "Viktor",
    "Vladimir",
    "Volibear",
    "Warwick",
    "Wukong",
    "Xayah",
    "Xerath",
    "Xin Zhao",
    "Yasuo",
    "Yone",
    "Yorick",
    "Yuumi",
    "Zac",
    "Zed",
    "Zeri",
    "Ziggs",
    "Zilean",
    "Zoe",
    "Zyra"
)