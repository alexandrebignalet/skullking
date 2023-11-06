package org.skull.king.core.domain.state

import org.skull.king.core.domain.Card
import org.skull.king.core.domain.SkullKingOverError


object OverState : Skullking("") {
    override val version: Int
        get() = -1

    override fun compose(e: SkullKingEvent, version: Int): Skullking = this
    override fun playCard(playerId: String, card: Card): CardPlayed {
        throw SkullKingOverError(this)
    }
}