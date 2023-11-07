package org.skull.king.domain.core

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
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.core.domain.*
import org.skull.king.core.domain.state.Started
import org.skull.king.core.usecases.*
import org.skull.king.core.usecases.captor.ReadCard
import java.time.Duration
import java.time.temporal.ChronoUnit

@QuarkusTest
class AnnounceStateTest {

    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var queryBus: QueryBus

    private val mockedCard = listOf(
        Mermaid(),
        SkullkingCard,
        Pirate(PirateName.TORTUGA_JACK),


        ColoredCard(1, CardColor.BLUE),
        ColoredCard(3, CardColor.BLUE),
        ColoredCard(2, CardColor.BLUE),

        ColoredCard(1, CardColor.RED),
        ColoredCard(3, CardColor.RED),
        ColoredCard(2, CardColor.RED),


        ColoredCard(1, CardColor.RED),
        ColoredCard(2, CardColor.RED),
        ColoredCard(3, CardColor.RED),

        ColoredCard(2, CardColor.BLUE),
        ColoredCard(1, CardColor.BLUE),
        ColoredCard(3, CardColor.BLUE),

        ColoredCard(1, CardColor.YELLOW),
        ColoredCard(2, CardColor.YELLOW),
        ColoredCard(3, CardColor.YELLOW),


        ColoredCard(4, CardColor.YELLOW),
        ColoredCard(2, CardColor.YELLOW),
        ColoredCard(3, CardColor.YELLOW),
        ColoredCard(1, CardColor.YELLOW),

        ColoredCard(4, CardColor.BLUE),
        ColoredCard(2, CardColor.BLUE),
        ColoredCard(3, CardColor.BLUE),
        ColoredCard(1, CardColor.BLUE),

        ColoredCard(4, CardColor.RED),
        ColoredCard(2, CardColor.RED),
        ColoredCard(3, CardColor.RED),
        ColoredCard(1, CardColor.RED),

        ColoredCard(4, CardColor.BLACK),
        ColoredCard(2, CardColor.BLACK),
        ColoredCard(3, CardColor.BLACK),
        ColoredCard(1, CardColor.BLACK)
    )

    private val players = listOf("1", "2", "3")
    private val gameId = "101"

    @BeforeEach
    fun setUp() {
        mockkConstructor(Deck::class)
        every { anyConstructed<Deck>().pop() } returnsMany (mockedCard)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(Deck::class)
    }

    @Test
    fun `Should serve cards to players and rotate first player when a new round begins`() {
        // Given
        val startedEvent = commandBus.send(StartSkullKing(gameId, players)).second.first() as Started
        val firstPlayer = startedEvent.players.first()
        val secondPlayer = startedEvent.players[1]
        val thirdPlayer = startedEvent.players.last()

        // When players finish the first and only fold of the round
        listOf(
            AnnounceWinningCardsFoldCount(gameId, firstPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, secondPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, thirdPlayer.id, 1)
        ).forEach(commandBus::send)

        listOf(
            PlayCardSaga(gameId, firstPlayer.id, mockedCard.first()),
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[1]),
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[2])
        ).forEach(commandBus::send)


        // Then

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val game = queryBus.send(GetGame(startedEvent.gameId))
            Assertions.assertThat(game?.roundNb).isEqualTo(2)
        }

        var game = queryBus.send(GetGame(startedEvent.gameId))


        var newFirstPlayer = queryBus.send(GetPlayer(game!!.id, secondPlayer.id))
        val newSecondPlayer = queryBus.send(GetPlayer(game.id, thirdPlayer.id))
        val newThirdPlayer = queryBus.send(GetPlayer(game.id, firstPlayer.id))

        // cards served
        Assertions.assertThat(newFirstPlayer.cards).contains(ReadCard.from(mockedCard[3]), ReadCard.from(mockedCard[6]))
        Assertions.assertThat(newSecondPlayer.cards)
            .contains(ReadCard.from(mockedCard[4]), ReadCard.from(mockedCard[7]))
        Assertions.assertThat(newThirdPlayer.cards).contains(ReadCard.from(mockedCard[5]), ReadCard.from(mockedCard[8]))
        // first player rotated
        Assertions.assertThat(game.currentPlayerId).isEqualTo(secondPlayer.id)

        listOf(
            AnnounceWinningCardsFoldCount(gameId, firstPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, secondPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, thirdPlayer.id, 1)
        ).forEach(commandBus::send)

        // first fold
        listOf(
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[3]),
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[4]),
            PlayCardSaga(gameId, firstPlayer.id, mockedCard[5])
        ).forEach(commandBus::send)

        // second player wins
        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val result = queryBus.send(GetGame(startedEvent.gameId))
            Assertions.assertThat(result?.currentPlayerId).isEqualTo(thirdPlayer.id)
        }

        game = queryBus.send(GetGame(startedEvent.gameId))
        Assertions.assertThat(game?.currentPlayerId).isEqualTo(thirdPlayer.id)

        // second & last fold
        listOf(
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[7]),
            PlayCardSaga(gameId, firstPlayer.id, mockedCard[8]),
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[6])
        ).forEach(commandBus::send)

        // round finished first player to start rotate
        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val result = queryBus.send(GetGame(startedEvent.gameId))
            Assertions.assertThat(result?.roundNb).isEqualTo(3)
        }

        game = queryBus.send(GetGame(startedEvent.gameId))
        Assertions.assertThat(game?.roundNb).isEqualTo(3)

        newFirstPlayer = queryBus.send(GetPlayer(game!!.id, thirdPlayer.id))

        Assertions.assertThat(game.currentPlayerId).isEqualTo(newFirstPlayer.id)

        listOf(
            AnnounceWinningCardsFoldCount(gameId, firstPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, secondPlayer.id, 1),
            AnnounceWinningCardsFoldCount(gameId, thirdPlayer.id, 1)
        ).forEach(commandBus::send)

        // first fold
        listOf(
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[9]),
            PlayCardSaga(gameId, firstPlayer.id, mockedCard[10]),
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[11])
        ).forEach(commandBus::send)

        // third player wins
        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val result = queryBus.send(GetGame(startedEvent.gameId))
            Assertions.assertThat(result?.currentPlayerId).isEqualTo(secondPlayer.id)
        }

        game = queryBus.send(GetGame(startedEvent.gameId))
        Assertions.assertThat(game?.currentPlayerId).isEqualTo(secondPlayer.id)

        // second fold
        listOf(
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[14]),
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[12]),
            PlayCardSaga(gameId, firstPlayer.id, mockedCard[13])
        ).forEach(commandBus::send)

        // third fold
        listOf(
            PlayCardSaga(gameId, secondPlayer.id, mockedCard[17]),
            PlayCardSaga(gameId, thirdPlayer.id, mockedCard[15]),
            PlayCardSaga(gameId, firstPlayer.id, mockedCard[16])
        ).forEach(commandBus::send)

        // round finished first player to start rotate
        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            val result = queryBus.send(GetGame(startedEvent.gameId))
            Assertions.assertThat(result?.roundNb).isEqualTo(4)
        }

        game = queryBus.send(GetGame(startedEvent.gameId))
        Assertions.assertThat(game?.roundNb).isEqualTo(4)

        newFirstPlayer = queryBus.send(GetPlayer(game!!.id, firstPlayer.id))

        Assertions.assertThat(game.currentPlayerId).isEqualTo(newFirstPlayer.id)
    }
}
