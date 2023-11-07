package org.skull.king.domain.core

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.domain.PlayerAlreadyAnnouncedError
import org.skull.king.core.domain.PlayerNotInGameError
import org.skull.king.core.domain.SkullKingNotStartedError
import org.skull.king.core.domain.state.PlayerAnnounced
import org.skull.king.core.domain.state.Started
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCount
import org.skull.king.core.usecases.GetGame
import org.skull.king.core.usecases.StartSkullKing
import java.time.Duration
import java.time.temporal.ChronoUnit

@QuarkusTest
class AnnounceWinningCardsFoldCountSagaTest {

    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var queryBus: QueryBus

    @Test
    fun `Should return an error if gameId not started`() {
        val gameId = IdGenerator().skullKingId()
        val playerId = "1"

        val command = AnnounceWinningCardsFoldCount(gameId, playerId, 5)

        Assertions.assertThatThrownBy { commandBus.send(command) }
            .isInstanceOf(SkullKingNotStartedError::class.java)
    }

    @Test
    fun `Should return an error if playerId not in game`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1", "2")
        val announcingPlayerId = "3"

        val startCommand = StartSkullKing(gameId, players)
        val announce = AnnounceWinningCardsFoldCount(gameId, announcingPlayerId, 5)

        commandBus.send(startCommand)

        Assertions.assertThatThrownBy { commandBus.send(announce) }
            .isInstanceOf(PlayerNotInGameError::class.java)
    }

    @Test
    fun `Should store the player announce`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1", "2")
        val announcingPlayerId = "1"
        val firstPlayerAnnounce = 5
        val roundNb = 1

        runBlocking {
            // COMMAND
            val start = StartSkullKing(gameId, players)
            val announce = AnnounceWinningCardsFoldCount(gameId, announcingPlayerId, firstPlayerAnnounce)

            commandBus.send(start)
            val result = commandBus.send(announce)

            val announced = result.second.first() as PlayerAnnounced
            Assertions.assertThat(announced.announce).isEqualTo(firstPlayerAnnounce)
            Assertions.assertThat(announced.playerId).isEqualTo(announcingPlayerId)
            Assertions.assertThat(announced.gameId).isEqualTo(gameId)
            Assertions.assertThat(announced.roundNb).isEqualTo(1)

            // QUERY
            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                val query = GetGame(gameId)
                val game = queryBus.send(query)!!
                Assertions.assertThat(game.id).isEqualTo(gameId)
                Assertions.assertThat(game.scoreBoard[announcingPlayerId]?.find { it.roundNb == roundNb }?.announced)
                    .isEqualTo(firstPlayerAnnounce)
            }
        }
    }

    @Test
    fun `Should return an error if playerId already announced for this turn`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1", "2")
        val announcingPlayerId = "1"

        val start = StartSkullKing(gameId, players)
        val announce = AnnounceWinningCardsFoldCount(gameId, announcingPlayerId, 5)
        val announceError = AnnounceWinningCardsFoldCount(gameId, announcingPlayerId, 2)

        commandBus.send(start)
        commandBus.send(announce)

        Assertions.assertThatThrownBy { commandBus.send(announceError) }
            .isInstanceOf(PlayerAlreadyAnnouncedError::class.java)
    }

    @Test
    fun `Should return an error if game is already ready`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1", "2")

        runBlocking {
            val start = StartSkullKing(gameId, players)
            val started = commandBus.send(start).second.first() as Started
            val firstPlayer = started.players.first()
            val secondPlayer = started.players.last()

            val firstAnnounce = AnnounceWinningCardsFoldCount(gameId, firstPlayer.id, 5)
            val secondAnnounce = AnnounceWinningCardsFoldCount(gameId, secondPlayer.id, 2)
            val errorAnnounce = AnnounceWinningCardsFoldCount(gameId, firstPlayer.id, 2)

            commandBus.send(firstAnnounce)
            commandBus.send(secondAnnounce)

            Assertions.assertThatThrownBy { commandBus.send(errorAnnounce) }
                .isInstanceOf(PlayerAlreadyAnnouncedError::class.java)
        }
    }
}
