package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.core.domain.state.PlayerAnnounced

@ApplicationScoped
class OnPlayerAnnounced(val repository: ReadSkullKingRepository) : EventCaptor<PlayerAnnounced> {

    override fun execute(event: PlayerAnnounced) {
        repository[event.aggregateId]
            ?.onPlayerAnnounced(event)
            ?.let(repository::save)
    }
}