package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.core.domain.state.Started

@ApplicationScoped
class OnGameStarted(val repository: ReadSkullKingRepository) : EventCaptor<Started> {

    override fun execute(event: Started) {
        ReadSkullKing.create(event)
            .let(repository::save)
    }
}