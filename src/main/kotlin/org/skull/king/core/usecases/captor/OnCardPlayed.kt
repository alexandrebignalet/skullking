package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.application.infrastructure.notification.EventPubSub.TopicId
import org.skull.king.core.domain.state.CardPlayed

@ApplicationScoped
class OnCardPlayed(val repository: ReadSkullKingRepository, val eventPubSub: EventPubSub) : EventCaptor<CardPlayed> {

    override fun execute(event: CardPlayed) {
        repository[event.aggregateId]
            ?.onCardPlayed(event)
            ?.let(repository::save)

        eventPubSub.publish(TopicId(event.gameId), event)
    }
}