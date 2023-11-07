package org.skull.king.domain

import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.Started
import org.skull.king.core.usecases.AnnounceWinningCardsFoldCountSaga
import org.skull.king.core.usecases.GetGame
import org.skull.king.core.usecases.PlayCardSaga
import org.skull.king.core.usecases.StartSkullKing
import org.skull.king.core.usecases.captor.RoundScore
import org.skull.king.core.usecases.captor.SkullKingPhase
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

@QuarkusTest
class ConcurrencyTest {
    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var queryBus: QueryBus

    private val mockedCard = listOf(
        Mermaid(),
        SkullkingCard,
        Pirate(PirateName.EVIL_EMMY),
        Pirate(PirateName.HARRY_THE_GIANT),
        Pirate(PirateName.TORTUGA_JACK),
        Mermaid(),

        ColoredCard(3, CardColor.BLUE),
        ColoredCard(8, CardColor.BLUE),
        ColoredCard(2, CardColor.BLUE),
        ColoredCard(2, CardColor.RED),
        ColoredCard(4, CardColor.RED),
        ColoredCard(5, CardColor.RED)
    )
    private val players = listOf("1", "2", "3", "4", "5", "6")
    private lateinit var firstPlayer: Player
    private lateinit var secondPlayer: Player
    private lateinit var thirdPlayer: Player
    private lateinit var forthPlayer: Player
    private lateinit var fifthPlayer: Player
    private lateinit var sixthPlayer: Player

    @BeforeEach
    fun setUp() {
        mockkConstructor(Deck::class)
        every { anyConstructed<Deck>().pop() } returnsMany (mockedCard)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(Deck::class)
    }

    @RepeatedTest(10)
    fun `Should handle correctly concurrent announcement as sequential announcement`() {
        val gameId = IdGenerator().skullKingId()
        val start = StartSkullKing(gameId, players)
        val startedEvent = commandBus.send(start).second.single() as Started

        firstPlayer = startedEvent.players.first()
        secondPlayer = startedEvent.players[1]
        thirdPlayer = startedEvent.players[2]
        forthPlayer = startedEvent.players[3]
        fifthPlayer = startedEvent.players[4]
        sixthPlayer = startedEvent.players.last()

        val announceCommands = players.map {
            AnnounceWinningCardsFoldCountSaga(gameId, it, 1)
        }

        val threads = announceCommands.map { command ->
            thread { commandBus.send(command) }
        }

        threads.forEach { it.join() }

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val game = queryBus.send(GetGame(gameId))
            Assertions.assertThat(game?.phase).isEqualTo(SkullKingPhase.CARDS)
            Assertions.assertThat(game?.scoreBoard).hasSize(players.size)
        }
    }

    @RepeatedTest(10)
    fun `Should handle correctly concurrent card play as sequential announcement`() {
        val gameId = IdGenerator().skullKingId()
        val start = StartSkullKing(gameId, players)
        val startedEvent = commandBus.send(start).second.single() as Started

        firstPlayer = startedEvent.players.first()
        secondPlayer = startedEvent.players[1]
        thirdPlayer = startedEvent.players[2]
        forthPlayer = startedEvent.players[3]
        fifthPlayer = startedEvent.players[4]
        sixthPlayer = startedEvent.players.last()

        startedEvent.players.forEach {
            commandBus.send(AnnounceWinningCardsFoldCountSaga(gameId, it.id, 1))
        }

        val commands = startedEvent.players.mapIndexed { index, player ->
            PlayCardSaga(gameId, player.id, mockedCard[index])
        }

        val threads = commands.map { command ->
            thread {
                retry {
                    commandBus.send(command)
                }
            }
        }

        threads.forEach { it.join() }

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val game = queryBus.send(GetGame(gameId))!!
            val winnerScore = game.scoreBoard[firstPlayer.id]?.find { it.roundNb == 1 }
            Assertions.assertThat(winnerScore).isEqualTo(
                RoundScore(
                    announced = 1,
                    done = 1,
                    potentialBonus = 50,
                    roundNb = 1
                )
            )
        }
    }

    private fun <T> retry(retry: Int = 1, block: () -> T) {
        try {
            block()
        } catch (e: Exception) {
            if (retry > 10) throw e
            Thread.sleep(50)
            retry(retry + 1, block)
        }
    }
}
