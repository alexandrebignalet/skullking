package org.skull.king.application.infrastructure.framework.infrastructure.bus.event

import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.ddd.event.EventBus
import org.skull.king.application.infrastructure.framework.ddd.event.EventBusMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventBusAsync(middlewares: Set<EventBusMiddleware>, captors: Set<EventCaptor<Event>>) : EventBus, Closeable {
    companion object {
        private val LOGGER: Logger = Logger.getLogger(EventBusAsync::class.java)
    }

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val queue = ConcurrentLinkedQueue<Event>()

    private val middlewareChain =
        middlewares.toList().foldRight(CaptorInvokation(captors.toList())) { current: EventBusMiddleware, next: Chain ->
            Chain(current, next)
        }

    override fun publish(events: Sequence<Event>) {
        queue.addAll(events)

        executorService.submit {
            while (queue.isNotEmpty()) {
                execute(queue.poll())
            }
        }
    }

    private fun execute(event: Event) = middlewareChain.apply(event)

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
                    (c as EventCaptor<Event>).execute(event)
                    true
                }
                .reduce { a, b -> a && b }
        }
    }

    override fun close() {
        runCatching {
            executorService.awaitTermination(3, TimeUnit.SECONDS)

        }
            .onSuccess {
                LOGGER.info("EventBusAsync terminated gracefully")
            }
            .onFailure {
                LOGGER.error("EventBusAsync failed terminating successfully - forcing it")
                executorService.shutdownNow()
            }
    }
}
