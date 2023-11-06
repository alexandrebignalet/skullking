package org.skull.king.application.infrastructure.framework.infrastructure.logger

import org.jboss.logging.Logger
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.command.CommandMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.application.infrastructure.framework.saga.Saga
import java.util.function.Supplier

class CommandHandlerLoggerMiddleware : CommandMiddleware {

    companion object {
        private val LOGGER = Logger.getLogger(CommandHandlerLoggerMiddleware::class.java)
    }


    override fun <T> intercept(
        bus: CommandBus,
        message: Command<T>,
        next: Supplier<Pair<T, Sequence<Event>>>
    ): Pair<T, Sequence<Event>> {

        if (message !is Saga) {
            LOGGER.info("Executing command $message")
        }

        return runCatching {
            val res = next.get()
            if (message !is Saga) {
                LOGGER.info("Success command execution ${message::class.java.simpleName} ${res.first} ${res.second.toList()}")
            }
            res
        }.getOrElse {
            LOGGER.info("Failure command execution ${message.javaClass.simpleName} $it")
            throw it
        }
    }
}