package org.skull.king.web.controller

import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.authentication.CookieCredentials
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.game_room.domain.GameUser
import org.skull.king.game_room.infrastructure.GameRoomService
import org.skull.king.game_room.infrastructure.web.CreateGameRoomResponse
import org.skull.king.game_room.infrastructure.web.GameRoomResource.StartResponse
import org.skull.king.helpers.ApiHelper

@QuarkusTest
class GameRoomResourceTest {

    companion object {
        private val defaultUser = User("uid", "francis")
        private val defaultGameUser = GameUser.from(defaultUser)

        @JvmStatic
        @BeforeAll
        fun mockAuthentication() {
            mockkConstructor(CookieCredentials::class)
            every { anyConstructed<CookieCredentials>().toUser() } returns defaultUser
        }

        @AfterEach
        fun tearDown() {
            unmockkConstructor(CookieCredentials::class)
        }
    }

    @Inject
    lateinit var gameRoomService: GameRoomService

    val api = ApiHelper()

    @Test
    fun `Should create a game room and add the game room for creator`() {
        val creator = defaultGameUser

        val response = api.gameRoom.create(defaultUser)

        val body = response.`as`(CreateGameRoomResponse::class.java)

        val gameRoom = gameRoomService.findOne(body.id)
        Assertions.assertThat(body.id).isEqualTo(gameRoom.id)
        Assertions.assertThat(gameRoom.creator).isEqualTo(creator.id)
        Assertions.assertThat(gameRoom.users).contains(creator)
    }

    @Test
    fun `Should allow players to join`() {
        val creator = GameUser("a_creator", "jean")
        val gameRoomId = gameRoomService.create(creator)

        // default user is the joiner
        api.gameRoom.join(User("10", "michel"), gameRoomId)

        val gameRoom = gameRoomService.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.users).contains(defaultGameUser)
    }

    @Test
    fun `Should not allow more than 6 people in the game room`() {
        val creator = GameUser("user_id", "jean")
        val gameRoomId = gameRoomService.create(creator)
        gameRoomService.join(gameRoomId, GameUser("2", "2"))
        gameRoomService.join(gameRoomId, GameUser("3", "3"))
        gameRoomService.join(gameRoomId, GameUser("4", "4"))
        gameRoomService.join(gameRoomId, GameUser("5", "5"))
        gameRoomService.join(gameRoomId, GameUser("6", "6"))

        val response = api.gameRoom.join(User("10", "michel"), gameRoomId)
        Assertions.assertThat(response.statusCode).isEqualTo(400)
    }

    @Test
    fun `Should not allow same person to join multiple times`() {
        val creator = defaultGameUser
        val gameRoomId = gameRoomService.create(creator)

        val response = api.gameRoom.join(defaultUser, gameRoomId)
        Assertions.assertThat(response.statusCode).isEqualTo(400)
    }

    @Test
    fun `Should start a game when the game room contains enough player`() {
        val creator = defaultGameUser
        val gameRoomId = gameRoomService.create(creator)
        gameRoomService.join(gameRoomId, GameUser("2", "2"))
        gameRoomService.join(gameRoomId, GameUser("3", "3"))

        val startResponse = api.gameRoom.launch(defaultUser, gameRoomId)
            .body.`as`(StartResponse::class.java)

        Assertions.assertThat(startResponse.gameId).isNotNull
        val gameRoom = gameRoomService.findOne(gameRoomId)
        Assertions.assertThat(gameRoom.gameId).isEqualTo(startResponse.gameId)
    }

    @Test
    fun `Should only let creator launch the game`() {
        val creator = GameUser("user_id", "hugues")
        val gameRoomId = gameRoomService.create(creator)
        gameRoomService.join(gameRoomId, defaultGameUser)
        gameRoomService.join(gameRoomId, GameUser("3", "michel"))

        val response = api.gameRoom.launch(User("3", "michel"), gameRoomId)

        Assertions.assertThat(response.statusCode).isEqualTo(403)
    }
}
