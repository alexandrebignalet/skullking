package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.core.domain.state.NewRoundStarted

@ApplicationScoped
class OnNewRoundStarted(val repository: ReadSkullKingRepository) : EventCaptor<NewRoundStarted> {

    override fun execute(event: NewRoundStarted) {
        repository[event.gameId]
            ?.onNewRoundStarted(event)
            ?.let(repository::save)
    }
}