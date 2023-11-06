package org.skull.king.application.infrastructure.framework.infrastructure.bus.event

import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.command.CommandMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.ddd.event.EventBus
import org.skull.king.application.infrastructure.framework.ddd.event.EventStore
import java.util.function.Supplier

class EventDispatcherMiddleware(
    private val eventBus: EventBus,
    private val eventStore: EventStore
) : CommandMiddleware {

    companion object {
        private val LOGGER = Logger.getLogger(EventDispatcherMiddleware::class.java)
    }

    override fun <T> intercept(
        bus: CommandBus,
        message: Command<T>,
        next: Supplier<Pair<T, Sequence<Event>>>
    ) = next.get().let {
        val events = it.second.toList()
        LOGGER.info("Dispatching ${it.second.toList()}")
        if (events.isNotEmpty()) {
            eventStore.save(it.second)
            eventBus.publish(it.second)
        }
        it
    }
}
