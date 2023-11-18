package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.application.infrastructure.notification.EventPubSub.TopicId
import org.skull.king.core.domain.state.GameFinished

@ApplicationScoped
class OnGameFinished(val repository: ReadSkullKingRepository, val eventPubSub: EventPubSub) :
    EventCaptor<GameFinished> {

    override fun execute(event: GameFinished) {
        repository[event.gameId]
            ?.onGameFinished()
            ?.let(repository::save)

        eventPubSub.publish(TopicId(event.gameId), event)
    }
}