package org.skull.king.core.usecases.captor

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.application.infrastructure.notification.EventPubSub.TopicId
import org.skull.king.core.domain.state.Started
import org.skull.king.game_room.infrastructure.repository.GameRoomInMemoryRepository

@ApplicationScoped
class OnGameStarted(
    val repository: ReadSkullKingRepository,
    val gameRoomInMemoryRepository: GameRoomInMemoryRepository,
    val eventPubSub: EventPubSub
) : EventCaptor<Started> {

    override fun execute(event: Started) {
        val gameRoom = gameRoomInMemoryRepository.findByGameId(event.gameId)
        ReadSkullKing.create(event, gameRoom)
            .let(repository::save)


        eventPubSub.publish(TopicId(event.gameId), event)
    }
}