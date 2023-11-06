package org.skull.king.game_room.domain

interface GameRoomRepository {

    fun save(gameRoom: GameRoom)
    fun findOne(gameRoomId: String): GameRoom?
    fun findByGameId(gameId: String): GameRoom?
    fun findAll(): List<GameRoom>
}
