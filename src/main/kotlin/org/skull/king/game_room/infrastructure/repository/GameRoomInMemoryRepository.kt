package org.skull.king.game_room.infrastructure.repository

import jakarta.inject.Singleton
import org.skull.king.game_room.domain.GameRoom
import org.skull.king.game_room.domain.GameRoomRepository

@Singleton
class GameRoomInMemoryRepository : GameRoomRepository {
    private var entities: MutableMap<String, GameRoom> = mutableMapOf()

    override fun save(gameRoom: GameRoom) {
        entities[gameRoom.id] = gameRoom
    }

    override fun findOne(gameRoomId: String): GameRoom? = entities[gameRoomId]

    override fun findByGameId(gameId: String): GameRoom? {
        return entities.values.firstOrNull { v -> v.gameId.equals(gameId) }
    }

    override fun findAll() = entities.values.toList()
}