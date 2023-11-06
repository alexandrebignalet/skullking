package org.skull.king.application.infrastructure.framework.infrastructure.bus.command

import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.application.infrastructure.framework.command.CommandMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.Event

class CommandBusSynchronous(
    middlewares: Set<CommandMiddleware>,
    handlers: Set<CommandHandler<out Command<*>, *>>
) : CommandBus {

    override fun <TResponse> send(message: Command<TResponse>): Pair<TResponse, Sequence<Event>> {
        return middlewareChain.apply(message)
    }

    private val middlewareChain: Chain = middlewares.toList()
        .foldRight(finalChain(handlers)) { current: CommandMiddleware, next: Chain -> Chain(current, next) }

    private fun finalChain(handlers: Set<CommandHandler<out Command<*>, *>>): Chain {
        return Chain(InvokeCommandHandlerMiddleware(handlers), null)
    }

    private inner class Chain(private val current: CommandMiddleware, private val next: Chain?) {

        fun <T> apply(command: Command<T>): Pair<T, Sequence<Event>> {
            return current.intercept(this@CommandBusSynchronous, command) { requireNotNull(next).apply(command) }
        }
    }
}
