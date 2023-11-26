package org.skull.king.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured.given
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import jakarta.ws.rs.core.MediaType
import org.skull.king.application.infrastructure.authentication.AUTH_COOKIE_NAME
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.game_room.domain.Configuration
import org.skull.king.game_room.infrastructure.web.CreateGameRoomResponse

class ApiHelper {

    val skullKing = SkullKingApiHelper()
    val gameRoom = GameRoomHelper()
    val authentication = Authentication()

    companion object {
        val objectMapper = ObjectMapper()
    }

    inner class SkullKingApiHelper {
        fun start(users: List<User>, configuration: Configuration? = null): Response {
            val creator = users.first()
            val (id) = gameRoom.create(creator, configuration).`as`(CreateGameRoomResponse::class.java)

            users.forEach { gameRoom.join(it, id) }

            return givenAuthenticatedRequest(creator)
                .`when`()
                .post("/skullking/game_rooms/${id}/launch")
        }

        fun announce(user: User, gameId: String, playerId: String, count: Int): Response =
            givenAuthenticatedRequest(user)
                .body("{ \"count\": $count }")
                .`when`()
                .post("/skullking/games/$gameId/players/$playerId/announce")
    }

    inner class GameRoomHelper {

        fun create(user: User, configuration: Configuration? = null): Response =
            givenAuthenticatedRequest(user)
                .body(objectMapper.writeValueAsString(configuration))
                .`when`()
                .post("/skullking/game_rooms")


        fun join(user: User, gameRoomId: String): Response =
            givenAuthenticatedRequest(user)
                .`when`()
                .post("/skullking/game_rooms/$gameRoomId/users")

        fun launch(user: User, gameRoomId: String): Response =
            givenAuthenticatedRequest(user)
                .`when`()
                .post("/skullking/game_rooms/$gameRoomId/launch")
    }

    private fun givenAuthenticatedRequest(user: User): RequestSpecification =
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .cookie(AUTH_COOKIE_NAME, user.name)

    inner class Authentication {
        fun register(userName: String): Response =
            given()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body("{ \"userName\": \"$userName\" }")
                .`when`()
                .post("/skullking/register")
    }
}
