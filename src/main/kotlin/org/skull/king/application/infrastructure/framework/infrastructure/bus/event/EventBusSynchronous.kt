package org.skull.king.application.infrastructure.framework.infrastructure.bus.event

import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.ddd.event.EventBus
import org.skull.king.application.infrastructure.framework.ddd.event.EventBusMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor


class EventBusSynchronous(middlewares: Set<EventBusMiddleware>, captors: Set<EventCaptor<Event>>) : EventBus {
    companion object {
        private val LOGGER: Logger = Logger.getLogger(EventBusSynchronous::class.java)
    }

    private val middlewareChain =
        middlewares.toList().foldRight(CaptorInvokation(captors.toList())) { current: EventBusMiddleware, next: Chain ->
            Chain(current, next)
        }

    override fun publish(events: Sequence<Event>) {
        events.forEach { event: Event -> execute(event) }
    }

    private fun execute(event: Event): Boolean {
        return middlewareChain.apply(event)
    }

    private open class Chain(private val current: EventBusMiddleware?, private val next: Chain?) {

        open fun apply(event: Event): Boolean {
            current?.intercept(event) { next?.apply(event) }
            return true
        }
    }

    private class CaptorInvokation(private val captors: List<EventCaptor<*>>) : Chain(null, null) {

        @Suppress("UNCHECKED_CAST")
        override fun apply(event: Event): Boolean {
            return captors
                .filter { c -> c.eventType() == event.javaClass }
                .map { c ->
                    LOGGER.info("Applying captor ${c.javaClass.simpleName} $event")
                    (c as EventCaptor<Event>).execute(event)
                    true
                }
                .reduce { a, b -> a && b }
        }
    }
}
