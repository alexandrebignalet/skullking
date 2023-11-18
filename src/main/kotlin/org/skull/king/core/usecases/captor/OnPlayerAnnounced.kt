package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.application.infrastructure.notification.EventPubSub.TopicId
import org.skull.king.core.domain.state.PlayerAnnounced

@ApplicationScoped
class OnPlayerAnnounced(val repository: ReadSkullKingRepository, val eventPubSub: EventPubSub) :
    EventCaptor<PlayerAnnounced> {

    override fun execute(event: PlayerAnnounced) {
        repository[event.aggregateId]
            ?.onPlayerAnnounced(event)
            ?.let(repository::save)

        eventPubSub.publish(TopicId(event.gameId), event)
    }
}