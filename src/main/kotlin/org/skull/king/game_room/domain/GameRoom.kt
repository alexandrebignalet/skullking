package org.skull.king.game_room.domain

import java.time.Instant

data class GameRoom(
    val id: String,
    val creator: String,
    val users: Set<GameUser>,
    val gameId: String? = null,
    val creationDate: Long = Instant.now().toEpochMilli(),
    val updateDate: Long = Instant.now().toEpochMilli(),
    val configuration: Configuration? = null
) {

    val bots: List<GameUser> = users.filter { it.type == GameUserType.BOT }

    fun isFull() = users.count() == 6

}
