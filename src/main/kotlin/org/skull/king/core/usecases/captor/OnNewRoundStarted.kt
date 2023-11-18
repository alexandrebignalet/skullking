package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.core.domain.state.RoundFinished

@ApplicationScoped
class OnNewRoundStarted(val repository: ReadSkullKingRepository, val eventPubSub: EventPubSub) :
    EventCaptor<RoundFinished> {

    override fun execute(event: RoundFinished) {
        repository[event.gameId]
            ?.onNewRoundStarted(event)
            ?.let(repository::save)

        eventPubSub.publish(EventPubSub.TopicId(event.gameId), event)
    }
}