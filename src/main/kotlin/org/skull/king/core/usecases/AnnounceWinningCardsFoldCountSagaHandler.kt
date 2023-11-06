package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.saga.RetryableSagaHandler

@ApplicationScoped
class AnnounceWinningCardsFoldCountSagaHandler : RetryableSagaHandler<String, AnnounceWinningCardsFoldCountSaga>() {

    override fun run(bus: CommandBus, saga: AnnounceWinningCardsFoldCountSaga): Pair<String, Sequence<Event>> {
        exponentialBackoff {
            bus.send(AnnounceWinningCardsFoldCount(saga.gameId, saga.playerId, saga.count))
        }

        return Pair(saga.gameId, sequenceOf())
    }
}
