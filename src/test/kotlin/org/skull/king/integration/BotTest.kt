package org.skull.king.integration

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.bot.domain.strategy.BotStrategy.BotStrategyType
import org.skull.king.core.domain.state.OverState
import org.skull.king.core.infrastructure.SkullkingRepository
import org.skull.king.game_room.domain.GameUser
import org.skull.king.game_room.domain.GameUser.BotGameUser
import org.skull.king.game_room.infrastructure.GameRoomService
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread

@QuarkusTest
class BotTest {
    @Inject
    lateinit var skullKingRepository: SkullkingRepository

    @Inject
    lateinit var gameRoomService: GameRoomService

    @Test
    fun `should play a full game with dumb bots only`() {
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
        runGameWithBots(gameRoomService, skullKingRepository, dumbBots())
    }

    @Test
    fun `should play a full game with nasus bots only`() {
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
        runGameWithBots(gameRoomService, skullKingRepository, nasusBots())
    }

    @Test
    fun `should play a full game with a mixed typed of bots`() {
        runGameWithBots(gameRoomService, skullKingRepository, bots())
        runGameWithBots(gameRoomService, skullKingRepository, bots())
        runGameWithBots(gameRoomService, skullKingRepository, bots())
        runGameWithBots(gameRoomService, skullKingRepository, bots())
        runGameWithBots(gameRoomService, skullKingRepository, bots())
        runGameWithBots(gameRoomService, skullKingRepository, bots())
    }

    private fun runGameWithBots(
        gameRoomService: GameRoomService,
        skullKingRepository: SkullkingRepository,
        bots: List<BotGameUser>
    ) {
        lateinit var gameId: String
        thread {
            val creator = bots.first()
            val gameRoomId = gameRoomService.create(creator)
            bots.filter { it != creator }.forEach { gameRoomService.join(gameRoomId, it) }
            gameId = gameRoomService.startGame(gameRoomId, creator.id)
        }

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            assertThat(skullKingRepository[gameId] is OverState).isTrue()
        }
    }

    private fun dumbBots() = listOf(
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot)
    )

    private fun nasusBots() = listOf(
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot)
    )

    private fun bots() = listOf(
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.Dumbot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot),
        GameUser.bot(IdGenerator().userId(), BotStrategyType.NasusBot)
    )
}
