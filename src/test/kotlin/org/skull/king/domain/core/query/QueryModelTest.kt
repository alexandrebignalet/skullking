package org.skull.king.domain.core.query

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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.domain.Deck
import org.skull.king.core.domain.Mermaid
import org.skull.king.core.domain.SkullkingCard
import org.skull.king.core.domain.state.Started
import org.skull.king.core.usecases.*
import org.skull.king.core.usecases.captor.SkullKingPhase
import java.time.Duration
import java.time.temporal.ChronoUnit

@QuarkusTest
class QueryModelTest {

    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var queryBus: QueryBus

    private val gameId = IdGenerator().skullKingId()
    private val players = listOf("1", "2")
    private val mockedCard = listOf(Mermaid(), SkullkingCard)

    @BeforeEach
    fun setUp() {
        mockkConstructor(Deck::class)
        every { anyConstructed<Deck>().pop() } returnsMany (mockedCard)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(Deck::class)
    }

    @Nested
    inner class GamePhase {
        @Test
        fun `Should mark read game as in announcement phase on game started`() {
            commandBus.send(StartSkullKing(gameId, players))

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.phase).isEqualTo(SkullKingPhase.ANNOUNCEMENT)
                }
            }
        }

        @Test
        fun `Should mark read game as in cards phase when all players announced`() {
            val started = commandBus.send(StartSkullKing(gameId, players)).second.single() as Started

            started.players.forEach {
                commandBus.send(AnnounceWinningCardsFoldCount(gameId, it.id, 0))
            }

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.phase).isEqualTo(SkullKingPhase.CARDS)
                }
            }
        }

        @Test
        fun `Should reset to ANNOUCEMENT phase on round finished`() {
            val started = commandBus.send(StartSkullKing(gameId, players)).second.single() as Started

            started.players.forEach {
                commandBus.send(AnnounceWinningCardsFoldCountSaga(gameId, it.id, 0))
            }

            commandBus.send(PlayCardSaga(gameId, started.players.first().id, mockedCard.first()))
            commandBus.send(PlayCardSaga(gameId, started.players.last().id, mockedCard.last()))

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.phase).isEqualTo(SkullKingPhase.ANNOUNCEMENT)
                }
            }
        }
    }

    @Nested
    inner class CurrentPlayer {
        @Test
        fun `Should mark read game with current player id when game is started`() {
            val started = commandBus.send(StartSkullKing(gameId, players)).second.single() as Started

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.currentPlayerId).isEqualTo(started.players.first().id)
                }
            }
        }

        @Test
        fun `Should update game current player id on each card played and on round finished`() {
            val started = commandBus.send(StartSkullKing(gameId, players)).second.single() as Started

            started.players.forEach {
                commandBus.send(AnnounceWinningCardsFoldCount(gameId, it.id, 0))
            }

            commandBus.send(PlayCardSaga(gameId, started.players.first().id, mockedCard.first()))

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.currentPlayerId).isEqualTo(started.players.last().id)
                }
            }

            // First started the previous round, now we change second starts
            commandBus.send(PlayCardSaga(gameId, started.players.last().id, mockedCard.last()))
            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                queryBus.send(GetGame(gameId)).let { game ->
                    Assertions.assertThat(game.currentPlayerId).isEqualTo(started.players.last().id)
                }
            }
        }
    }
}
