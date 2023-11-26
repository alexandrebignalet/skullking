package org.skull.king.game_room.domain

import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.bot.domain.strategy.BotStrategy
import org.skull.king.bot.domain.strategy.BotStrategy.BotStrategyType

sealed interface GameUser {
    val id: String
    val name: String
    val rooms: Set<GameRoom>
    val type: GameUserType

    data class BotGameUser(
        override val id: String,
        override val name: String,
        override val rooms: Set<GameRoom> = setOf(),
        override val type: GameUserType = GameUserType.BOT,
        val strategy: BotStrategy
    ) : GameUser

    data class RealGameUser(
        override val id: String,
        override val name: String,
        override val rooms: Set<GameRoom> = setOf(),
        override val type: GameUserType = GameUserType.REAL
    ) : GameUser

    companion object {
        fun bot(id: String, strategy: BotStrategyType = BotStrategyType.Dumbot) =
            BotStrategy.from(strategy).let {
                BotGameUser(
                    id = id,
                    name = it.name(),
                    rooms = setOf(),
                    type = GameUserType.BOT,
                    strategy = it
                )
            }

        fun from(creator: User) = RealGameUser(
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
