package org.skull.king.web.controller

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.RepeatedTest
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.usecases.GetGame
import org.skull.king.core.usecases.captor.SkullKingPhase
import org.skull.king.game_room.infrastructure.web.GameRoomResource.StartResponse
import org.skull.king.helpers.ApiHelper
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

@QuarkusTest
class ConcurrencyIT {

    @Inject
    lateinit var queryBus: QueryBus
    private val api = ApiHelper()

    private val userOne = User("1", "johnny")
    private val userTwo = User("2", "johnny")
    private val userThree = User("3", "johnny")
    private val userFour = User("4", "johnny")
    private val userFive = User("5", "johnny")
    private val userSix = User("6", "johnny")
    private val users = listOf(
        userOne,
        userTwo,
        userThree,
        userFour,
        userFive,
        userSix
    )
    private val players = listOf("1", "2", "3", "4", "5", "6")

    @RepeatedTest(10)
    fun `Should handle correctly concurrent announcement as sequential announcement`() {
        val response = api.skullKing.start(users)
        val (gameId) = response.body.`as`(StartResponse::class.java)

        val threads = players.map { playerId ->
            val user = users.first { it.id == playerId }
            thread { api.skullKing.announce(user, gameId, playerId, 1) }
        }

        threads.forEach { it.join() }

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val game = queryBus.send(GetGame(gameId))
            Assertions.assertThat(game.phase).isEqualTo(SkullKingPhase.CARDS)
            Assertions.assertThat(game.scoreBoard).hasSize(players.size)
        }
    }
}
