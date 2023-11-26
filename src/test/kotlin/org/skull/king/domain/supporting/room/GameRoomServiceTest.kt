package org.skull.king.domain.supporting.room

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.ws.rs.ForbiddenException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.core.domain.ClassicConfiguration
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.domain.GameLauncher
import org.skull.king.game_room.domain.AlreadyInGameRoomException
import org.skull.king.game_room.domain.Configuration
import org.skull.king.game_room.domain.GameRoomFullException
import org.skull.king.game_room.domain.GameUser.RealGameUser
import org.skull.king.game_room.infrastructure.GameRoomService
import org.skull.king.game_room.infrastructure.repository.GameRoomInMemoryRepository

class GameRoomServiceTest {

    private val repository = GameRoomInMemoryRepository()
    private val gameLauncher = mockk<GameLauncher>()
    private val idGenerator = mockk<IdGenerator>()
    private val service = GameRoomService(repository, gameLauncher, idGenerator)

    private val gameRoomId = IdGenerator().gameRoomId()
    private val skullKingId = IdGenerator().skullKingId()

    @BeforeEach
    fun setUp() {
        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId
    }


    @Test
    fun `Should create a game room and add the game room for creator`() {
        val creator = RealGameUser("user_id", "toto")

        val gameRoomId = service.create(creator)

        val gameRoom = service.findOne(gameRoomId)
        Assertions.assertThat(gameRoomId).isEqualTo(gameRoom.id)
        Assertions.assertThat(gameRoom.creator).isEqualTo(creator.id)
        Assertions.assertThat(gameRoom.users).contains(creator)
    }

    @Test
    fun `Should allow players to join`() {
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)

        val otherUser = RealGameUser("another_user_id", "tata")
        service.join(gameRoomId, otherUser)

        val gameRoom = service.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.users).contains(otherUser)
    }

    @Test
    fun `Should not allow more than 6 people in the game room`() {
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)
        service.join(gameRoomId, RealGameUser("2", "2"))
        service.join(gameRoomId, RealGameUser("3", "3"))
        service.join(gameRoomId, RealGameUser("4", "4"))
        service.join(gameRoomId, RealGameUser("5", "5"))
        service.join(gameRoomId, RealGameUser("6", "6"))

        Assertions.assertThatThrownBy { service.join(gameRoomId, RealGameUser("7", "7")) }
            .isInstanceOf(GameRoomFullException::class.java)
    }

    @Test
    fun `Should not allow same person to join multiple times`() {
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)

        Assertions.assertThatThrownBy { service.join(gameRoomId, creator) }
            .isInstanceOf(AlreadyInGameRoomException::class.java)
    }

    @Test
    fun `Should start a game when the game room contains enough player`() {
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)
        service.join(gameRoomId, RealGameUser("2", "2"))
        service.join(gameRoomId, RealGameUser("3", "3"))
        val expectedGameId = slot<String>()
        every { gameLauncher.startFrom(capture(expectedGameId), any(), any()) } returns Unit

        val gameId = service.startGame(gameRoomId, creator.id)

        Assertions.assertThat(gameId).isEqualTo(expectedGameId.captured)
        val gameRoom = service.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.gameId).isEqualTo(gameId)
        verify { gameLauncher.startFrom(expectedGameId.captured, setOf(creator.id, "2", "3"), ClassicConfiguration()) }
    }

    @Test
    fun `Should only let creator launch the game`() {
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)
        service.join(gameRoomId, RealGameUser("2", "2"))
        service.join(gameRoomId, RealGameUser("3", "3"))


        Assertions.assertThatThrownBy { service.startGame(gameRoomId, "2") }
            .isInstanceOf(ForbiddenException::class.java)
        Assertions.assertThatThrownBy { service.startGame(gameRoomId, "12") }
            .isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `Should return an error if game failed to start`() {
        every { gameLauncher.startFrom(any(), any(), any()) } throws Error("game failed to start")
        val creator = RealGameUser("user_id", "toto")
        val gameRoomId = service.create(creator)
        service.join(gameRoomId, RealGameUser("2", "2"))
        service.join(gameRoomId, RealGameUser("3", "3"))

        Assertions.assertThatThrownBy { service.startGame(gameRoomId, creator.id) }
            .isInstanceOf(Error::class.java)

        val gameRoom = service.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.gameId).isNull()
    }

    @Test
    fun `should allow creator choose a variant`() {
        val creator = RealGameUser("user_id", "toto")

        val configuration = Configuration(false, false, false)
        val gameRoomId = service.create(creator, configuration)

        val gameRoom = service.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.configuration).isEqualTo(configuration)
    }

    @Test
    fun `Should start a game with the variant chosen`() {
        val creator = RealGameUser("user_id", "toto")

        val configuration = Configuration(true, true, true)
        val gameRoomId = service.create(creator, configuration)
        service.join(gameRoomId, RealGameUser("2", "2"))
        service.join(gameRoomId, RealGameUser("3", "3"))
        val expectedGameId = slot<String>()

        every { gameLauncher.startFrom(capture(expectedGameId), any(), any()) } returns Unit

        service.startGame(gameRoomId, creator.id)

        verify {
            gameLauncher.startFrom(
                expectedGameId.captured,
                setOf(creator.id, "2", "3"),
                GameConfiguration.from(configuration)
            )
        }
    }
}
