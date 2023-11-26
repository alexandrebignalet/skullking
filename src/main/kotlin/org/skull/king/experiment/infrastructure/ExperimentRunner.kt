package org.skull.king.experiment.infrastructure

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.bot.domain.strategy.BotStrategy.BotStrategyType
import org.skull.king.core.usecases.captor.ReadSkullKingRepository
import org.skull.king.experiment.domain.Experiment
import org.skull.king.experiment.domain.ExperimentRepository
import org.skull.king.game_room.domain.GameUser
import org.skull.king.game_room.infrastructure.GameRoomService

@ApplicationScoped
class ExperimentRunner(
    val skullKingRepository: ReadSkullKingRepository,
    val gameRoomService: GameRoomService,
    val idGenerator: IdGenerator,
    val experimentRepository: ExperimentRepository
) {

    @Scheduled(every = "300s")
    fun scheduleExperiment() {

        (2..6).map {
            val dumb = it / 2
            Experiment(
                id = idGenerator.experimentId(),
                playersCount = it,
                botRepartition = mapOf(
                    BotStrategyType.Dumbot to dumb,
                    BotStrategyType.NasusBot to it - dumb
                )
            )
        }
            .map { experiment ->
                experiment to experiment.allocateBotStrategies().map { GameUser.bot(idGenerator.botId(), it) }
            }
            .forEach { (experiment, bots) ->
                val skullId = launchSkullKing(bots)
                val skullKing = skullKingRepository[skullId]

                experimentRepository.save(experiment.copy(skullKingId = skullId))
            }
    }

    private fun launchSkullKing(bots: List<GameUser.BotGameUser>): String {
        val creator = bots.first()
        val gameRoomId = gameRoomService.create(creator)
        bots.filter { it != creator }.forEach { gameRoomService.join(gameRoomId, it) }
        return gameRoomService.startGame(gameRoomId, creator.id)
    }
}
