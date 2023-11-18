package org.skull.king.application.infrastructure.notification

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.websocket.SendResult
import jakarta.websocket.Session
import org.jboss.logging.Logger
import org.skull.king.core.domain.state.SkullKingEvent

@Singleton
class EventPubSub(
    @Inject var objectMapper: ObjectMapper,
) {
    data class TopicId(val value: String)

    private val topics: MutableMap<TopicId, List<Session>> = mutableMapOf()

    companion object {
        private val logger = Logger.getLogger(EventPubSub::class.java)
    }


    fun subscribe(topicId: TopicId, session: Session) {
        topics[topicId] = (topics[topicId] ?: listOf()) + session

        logger.info("Target subscribe to ${topicId}; ${topics[topicId]?.size ?: 0} subscription active on this topic")
    }

    fun publish(topicId: TopicId, event: SkullKingEvent) {
        val targets = topics[topicId] ?: return

        val payload = objectMapper.writeValueAsString(event)

        var eventsSent = 0
        targets.forEach {
            it.asyncRemote.sendObject(payload) { result: SendResult ->
                if (result.exception != null) {
                    logger.error("Unable to send message via ws - topicId=[${topicId.value}]; event=[$event]; error=[${result.exception}]")
                } else {
                    eventsSent++
                }
            }
        }
        logger.info("Publishing ${event.type}:${payload} to $eventsSent targets")
    }

    fun unsubscribe(topicId: TopicId, session: Session) {
        topics[topicId] = (topics[topicId] ?: listOf()) - session

        logger.info("Target unsubscribe to ${topicId}; ${topics[topicId]?.size ?: 0} subscription active on this topic")
    }

}
