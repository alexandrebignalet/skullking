package org.skull.king.domain.core

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.Started
import org.skull.king.core.usecases.GetGame
import org.skull.king.core.usecases.StartSkullKing
import java.time.Duration
import java.time.temporal.ChronoUnit

@QuarkusTest
class StartSkullKingTest {

    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var queryBus: QueryBus

    @Test
    fun `Should return an error if less than two players start the game`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1")

        Assertions.assertThatThrownBy { commandBus.send(StartSkullKing(gameId, players, ClassicConfiguration())) }
            .isInstanceOf(SkullKingConfigurationError::class.java)
    }

    @Test
    fun `Should return an error if more than 6 players start the game`() {
        val gameId = IdGenerator().skullKingId()
        val players = listOf("1", "2", "3", "4", "5", "6", "7")

        Assertions.assertThatThrownBy { commandBus.send(StartSkullKing(gameId, players, ClassicConfiguration())) }
            .isInstanceOf(SkullKingConfigurationError::class.java)
    }

    @Nested
    inner class StartSkullKingTest {
        private val gameId = IdGenerator().skullKingId()
        private val players = listOf("1", "2", "3", "4", "5")
        private lateinit var response: Pair<String, Sequence<Event>>

        @BeforeEach
        fun setUp() {
            response = commandBus.send(
                StartSkullKing(
                    gameId, players, ClassicConfiguration(deckShuffling = false)
                )
            )
        }

        @Test
        fun `Should start correctly the game`() {
            val gameStarted = response.second.first() as Started
            Assertions.assertThat(gameStarted.gameId).isEqualTo(gameId)

            val createdPlayers = gameStarted.players
            createdPlayers.forEach { Assertions.assertThat(it.gameId).isEqualTo(gameId) }
            Assertions.assertThat(createdPlayers.map(Player::id)).containsAll(players)
        }

        @Test
        fun `Should respect player ordering during card distribution`() {

            val gameStarted = response.second.first() as Started

            val createdPlayers = gameStarted.players

            // Dealer serve himself last
            val dealer = createdPlayers.last()
            Assertions.assertThat(dealer.cards.first()).isEqualTo(Pirate(PirateName.TORTUGA_JACK))

            val firstPlayer = createdPlayers[0]
            Assertions.assertThat(firstPlayer.cards.first()).isEqualTo(ScaryMary(ScaryMaryUsage.NOT_SET))

            val secondPlayer = createdPlayers[1]
            Assertions.assertThat(secondPlayer.cards.first()).isEqualTo(SkullkingCard)

            val thirdPlayer = createdPlayers[2]
            Assertions.assertThat(thirdPlayer.cards.first()).isEqualTo(Pirate(PirateName.BETTY_BRAVE))

            val forthPlayer = createdPlayers[3]
            Assertions.assertThat(forthPlayer.cards.first()).isEqualTo(Pirate(PirateName.BADEYE_JOE))
        }

        @Test
        fun `Should set the first player as current player before announcement`() {
            val gameStarted = response.second.first() as Started

            await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
                val game = queryBus.send(GetGame(gameId))!!
                Assertions.assertThat(game.currentPlayerId).isEqualTo(gameStarted.players.first().id)
            }
        }
    }
}
