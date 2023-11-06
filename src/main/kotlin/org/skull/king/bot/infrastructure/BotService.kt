package org.skull.king.bot.infrastructure

import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.inject.Singleton
import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.bot.domain.Bot
import org.skull.king.core.domain.state.OverState
import org.skull.king.core.domain.state.SkullKingEvent
import org.skull.king.core.infrastructure.SkullkingRepository
import org.skull.king.game_room.domain.GameRoomRepository
import java.io.Closeable

@Singleton
class BotService(
    private val skullkingRepository: SkullkingRepository,
    private val gameRoomRepository: GameRoomRepository,
    private val commandBus: CommandBus
) : Closeable {

    companion object {
        val logger: Logger = Logger.getLogger(BotService::class.java)
    }

    private val input = PublishSubject.create<SkullKingEvent>()
    private val subscription = input
        .observeOn(Schedulers.single())
        .subscribeOn(Schedulers.single())
        .map(this::retrieveBotsForGame)
        .flatMap { it.toObservable() }
        .subscribe(this::triggerBot)

    fun listen(event: SkullKingEvent) = input.onNext(event)

    override fun close() = subscription.dispose()

    private fun retrieveBotsForGame(event: SkullKingEvent) =
        gameRoomRepository.findByGameId(event.aggregateId)?.let { Bot.from(it, event) } ?: listOf()

    private fun triggerBot(bot: Bot) {
        val game = skullkingRepository[bot.trigger.aggregateId]

        if (game is OverState) {
            return
        }

        bot.withContext(game).command()
            .onSuccess { command ->
                logger.info("Bot ${bot.gameUser.name} doing $command on ${bot.trigger::class.simpleName} for ${game::class.simpleName}:${game.version}")
                commandBus.send(command)
            }
            .onFailure(logger::info)
    }
}