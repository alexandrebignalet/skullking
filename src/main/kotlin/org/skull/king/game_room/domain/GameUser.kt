package org.skull.king.game_room.domain

import org.skull.king.application.infrastructure.authentication.User

data class GameUser(
    val id: String,
    val name: String,
    val rooms: Set<GameRoom> = setOf(),
    val type: GameUserType = GameUserType.REAL
) {
    companion object {
        fun bot(id: String, name: String = "Nasus bot") = GameUser(
            id = id,
            name = name,
            rooms = setOf(),
            type = GameUserType.BOT
        )

        fun from(creator: User) = GameUser(
            id = creator.id,
            name = creator.userName,
            rooms = setOf(),
            type = GameUserType.REAL
        )
    }

}

enum class GameUserType {
    REAL,
    BOT
}
