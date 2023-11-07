package org.skull.king.game_room.infrastructure

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.NotFoundException
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.domain.GameLauncher
import org.skull.king.game_room.domain.*

@ApplicationScoped
class GameRoomService(
    private val repository: GameRoomRepository,
    private val launcher: GameLauncher,
    private val idGenerator: IdGenerator
) {

    fun create(creator: GameUser, configuration: Configuration? = null) =
        GameRoom(
            id = idGenerator.gameRoomId(),
            creator = creator.id,
            users = setOf(creator),
            configuration = configuration
        )
            .also { repository.save(it) }.id

    fun findOne(gameRoomId: String): GameRoom =
        repository.findOne(gameRoomId) ?: throw NotFoundException("Game room $gameRoomId do not exist")

    fun join(gameRoomId: String, user: GameUser) {
        val gameRoom = findOne(gameRoomId)

        if (gameRoom.isFull())
            throw GameRoomFullException(gameRoomId)

        val newUsers = gameRoom.users + user

        if (newUsers.count() == gameRoom.users.count()) throw AlreadyInGameRoomException(user.id, gameRoomId)

        gameRoom.copy(users = newUsers).let { repository.save(it) }
    }

    fun startGame(gameRoomId: String, userIdGameStarter: String): String {
        val gameRoom = findOne(gameRoomId)

        if (userIdGameStarter != gameRoom.creator) throw ForbiddenException("Only game room creator can start the game")

        val gameId = idGenerator.skullKingId()

        gameRoom.copy(gameId = gameId).let(repository::save)

        runCatching {
            launcher.startFrom(
                gameId,
                gameRoom.users.map(GameUser::id).toSet(),
                GameConfiguration.from(gameRoom.configuration)
            )
        }.onFailure {
            gameRoom.copy(gameId = null).let(repository::save)
            throw it
        }



        return gameId
    }

    fun findAll() = repository.findAll()
    fun findOneBy(gameId: String): GameRoom? {
        return repository.findByGameId(gameId)
    }
}
