package org.skull.king.integration

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.core.domain.state.OverState
import org.skull.king.core.infrastructure.SkullkingRepository
import org.skull.king.game_room.domain.GameUser
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
    fun `should play a full game with bots only`() {
        runGameWithBots(gameRoomService, skullKingRepository)
        runGameWithBots(gameRoomService, skullKingRepository)
        runGameWithBots(gameRoomService, skullKingRepository)
        runGameWithBots(gameRoomService, skullKingRepository)
        runGameWithBots(gameRoomService, skullKingRepository)
        runGameWithBots(gameRoomService, skullKingRepository)
    }

    private fun runGameWithBots(
        gameRoomService: GameRoomService,
        skullKingRepository: SkullkingRepository,
    ) {
        lateinit var gameId: String
        val idGenerator = IdGenerator()
        thread {
            val nasusBot = GameUser.bot(idGenerator.botId())
            val asheBot = GameUser.bot(idGenerator.botId())
            val xinBot = GameUser.bot(idGenerator.botId())

            val gameRoomId = gameRoomService.create(nasusBot)
            gameRoomService.join(gameRoomId, asheBot)
            gameRoomService.join(gameRoomId, xinBot)
            gameId = gameRoomService.startGame(gameRoomId, nasusBot.id)
        }

        await atMost Duration.of(5, ChronoUnit.SECONDS) untilAsserted {
            assertThat(skullKingRepository[gameId] is OverState).isTrue()
        }
    }
}
