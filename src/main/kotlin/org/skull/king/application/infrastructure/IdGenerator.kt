package org.skull.king.application.infrastructure

import com.github.f4b6a3.ksuid.KsuidCreator
import jakarta.enterprise.context.ApplicationScoped

const val SKULLKING_ID_PREFIX = "skg_"
const val USER_ID_PREFIX = "usr_"
const val GAME_ROOM_ID_PREFIX = "gr_"
const val BOT_ID_PREFIX = "bot_"

@ApplicationScoped
class IdGenerator {

    fun skullKingId() = SKULLKING_ID_PREFIX + KsuidCreator.getKsuid()
    fun userId() = USER_ID_PREFIX + KsuidCreator.getKsuid()
    fun gameRoomId() = GAME_ROOM_ID_PREFIX + KsuidCreator.getKsuid()
    fun botId() = BOT_ID_PREFIX + KsuidCreator.getKsuid()
}