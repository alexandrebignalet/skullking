package org.skull.king.application.infrastructure.framework.infrastructure.logger

import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.ddd.event.EventBusMiddleware

class EventCaptorLoggerMiddleware : EventBusMiddleware {

    companion object {
        private val LOGGER = Logger.getLogger(EventCaptorLoggerMiddleware::class.java)
    }

    override fun intercept(event: Event, next: Runnable) {
        return runCatching {
            next.run()
        }.getOrElse {
            LOGGER.info("Failure captor execution $it")
            throw it
        }
    }

}