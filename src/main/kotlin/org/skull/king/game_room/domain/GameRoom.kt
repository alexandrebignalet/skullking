package org.skull.king.game_room.domain

import io.quarkus.qute.TemplateData
import org.skull.king.application.infrastructure.authentication.User
import java.time.Instant

@TemplateData
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

    fun joinUrl() = "game_rooms/$id/join"
    fun addBotUrl() = "game_rooms/$id/bots"
    fun skullKingUrl() = "games/$gameId"
    fun isStarted() = gameId != null
    fun canJoin(user: User) = !isStarted() && !users.map { it.id }.contains(user.id)
    fun canStartSkullKing() = !isStarted() && users.size >= 2
    fun startUrl() = "game_rooms/$id/launch"
    fun userNameOf(playerId: String) = users.find { it.id == playerId }?.name
}
