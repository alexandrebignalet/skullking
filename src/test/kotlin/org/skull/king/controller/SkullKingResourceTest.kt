package org.skull.king.controller

import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.AUTH_COOKIE_NAME
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.application.web.exception.BaseErrorMessage
import org.skull.king.core.domain.*
import org.skull.king.core.usecases.GetGame
import org.skull.king.game_room.infrastructure.web.GameRoomResource.StartResponse
import org.skull.king.helpers.ApiHelper

@QuarkusTest
class SkullKingResourceTest {

    private val userOne = User(IdGenerator().userId(), "nasus")
    private val userTwo = User(IdGenerator().userId(), "ashe")
    private val userThree = User(IdGenerator().userId(), "xin")
    private val userFour = User(IdGenerator().userId(), "morde")
    private val userFive = User(IdGenerator().userId(), "jinx")
    private val userSix = User(IdGenerator().userId(), "poppy")
    private val users = listOf(
        userOne,
        userTwo,
        userThree,
        userFour,
        userFive,
        userSix
    )

    @Inject
    lateinit var queryBus: QueryBus

    @InjectMock
    lateinit var idGenerator: IdGenerator

    val api = ApiHelper()

    private val mockedCard = listOf(
        Mermaid(),
        SkullkingCard,
        ColoredCard(1, CardColor.BLUE)
    )

    @BeforeEach
    fun setUp() {
        mockkConstructor(Deck::class)
        every { anyConstructed<Deck>().pop() } returnsMany mockedCard
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(Deck::class)
    }

    @Test
    fun `Should start a new game with some players`() {
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId

        val commandResponse = api.skullKing.start(users)
            .body.`as`(StartResponse::class.java)

        Assertions.assertThat(commandResponse).isEqualTo(StartResponse(skullKingId))
    }

    @Test
    fun `Should return a bad request if less than 2 players to start`() {
        // Given
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId

        val response = api.skullKing.start(listOf(userOne))
        // Then
        Assertions.assertThat(response.statusCode).isEqualTo(400)
    }

    @Test
    fun `Should let player bet on its fold count`() {
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId
        // Given
        val (gameId) = api.skullKing.start(users).body.`as`(StartResponse::class.java)

        // When
        val commandResponse = api.skullKing.announce(userThree, gameId, userThree.id, 0)


        // Then
        Assertions.assertThat(commandResponse.statusCode).isEqualTo(204)
    }

    @Test
    fun `Should return an error if count below 0`() {
        // Given
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId

        val (gameId) = api.skullKing.start(users).`as`(StartResponse::class.java)

        // When
        val commandResponse = api.skullKing.announce(userOne, gameId, userOne.id, -5)

        // Then
        Assertions.assertThat(commandResponse.statusCode).isEqualTo(400)
        Assertions.assertThat(commandResponse.`as`(BaseErrorMessage::class.java))
            .isEqualTo(BaseErrorMessage("Announce must be greater than 0 and lower than 10", "INTERNAL"))
    }

    @Test
    fun `Should return an error if count above 10`() {
        // Given
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId

        val (gameId) = api.skullKing.start(users).`as`(StartResponse::class.java)

        // When
        val commandResponse = api.skullKing.announce(userOne, gameId, userOne.id, 15)

        // Then
        Assertions.assertThat(commandResponse.statusCode).isEqualTo(400)
        Assertions.assertThat(commandResponse.`as`(BaseErrorMessage::class.java))
            .isEqualTo(BaseErrorMessage("Announce must be greater than 0 and lower than 10", "INTERNAL"))
    }

    @Test
    fun `Should allow card play`() {
        // Given
        val gameRoomId = IdGenerator().gameRoomId()
        val skullKingId = IdGenerator().skullKingId()

        every { idGenerator.gameRoomId() } returns gameRoomId
        every { idGenerator.skullKingId() } returns skullKingId

        val (gameId) = api.skullKing.start(users).`as`(StartResponse::class.java)
        users.forEach { api.skullKing.announce(it, gameId, it.id, 1) }
        val game = queryBus.send(GetGame(gameId))
        val currentPlayerId = game?.currentPlayerId

        // When
        val request = """{
            "card": {
                "type": "MERMAID",
                "id": "MERMAID_NONE"
            }    
        }""".trimIndent()

        val currentUser = users.first { it.id == currentPlayerId }
        val commandResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .cookie(AUTH_COOKIE_NAME, currentUser.name)
            .body(request)
            .post("/skullking/games/$gameId/players/$currentPlayerId/play")


        // Then
        Assertions.assertThat(commandResponse.statusCode).isEqualTo(204)
    }
}
