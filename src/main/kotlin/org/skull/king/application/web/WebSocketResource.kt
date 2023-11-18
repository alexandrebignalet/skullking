package org.skull.king.application.web

import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint
import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.notification.EventPubSub
import org.skull.king.application.infrastructure.notification.EventPubSub.TopicId


@ServerEndpoint("/skullKing/{skullId}/subscribe")
@ApplicationScoped
class WebSocketResource(
    var eventPubSub: EventPubSub
) {
    companion object {
        private val logger = Logger.getLogger(WebSocketResource::class.java)
    }

    @OnOpen
    fun onOpen(session: Session, @PathParam("skullId") skullId: String) {
        eventPubSub.subscribe(TopicId(skullId), session)
    }

    @OnClose
    fun onClose(session: Session, @PathParam("skullId") skullId: String) {
        eventPubSub.unsubscribe(TopicId(skullId), session)
    }

    @OnError
    fun onError(session: Session, @PathParam("skullId") skullId: String, throwable: Throwable) {
        eventPubSub.unsubscribe(TopicId(skullId), session).also {
            logger.error("Error on websocket - session=[$session]; skullId=[$skullId]; error=[$throwable]")
        }
    }
}