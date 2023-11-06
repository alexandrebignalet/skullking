package org.skull.king.application.producers

import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Instance
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.application.infrastructure.framework.command.CommandMiddleware
import org.skull.king.application.infrastructure.framework.ddd.event.*
import org.skull.king.application.infrastructure.framework.infrastructure.bus.command.CommandBusSynchronous
import org.skull.king.application.infrastructure.framework.infrastructure.bus.event.EventBusAsync
import org.skull.king.application.infrastructure.framework.infrastructure.bus.event.EventDispatcherMiddleware
import org.skull.king.application.infrastructure.framework.infrastructure.bus.query.QueryBusSynchronous
import org.skull.king.application.infrastructure.framework.infrastructure.logger.CommandHandlerLoggerMiddleware
import org.skull.king.application.infrastructure.framework.infrastructure.logger.EventCaptorLoggerMiddleware
import org.skull.king.application.infrastructure.framework.query.QueryBus
import org.skull.king.application.infrastructure.framework.query.QueryHandler
import org.skull.king.application.infrastructure.framework.query.QueryMiddleware
import org.skull.king.application.infrastructure.framework.saga.Saga
import org.skull.king.application.infrastructure.framework.saga.SagaHandler
import org.skull.king.application.infrastructure.framework.saga.SagaMiddleware

@Dependent
class FrameworkBeansProducer {

    @DefaultBean
    @Named
    @Suppress("UNCHECKED_CAST")
    fun provideCommandMiddlewares(
        eventStore: EventStore,
        eventBus: Instance<EventBus>,
        sagaHandlers: Instance<SagaHandler<*, *>>
    ): Set<CommandMiddleware> = setOf(
        CommandHandlerLoggerMiddleware(),
        SagaMiddleware(sagaHandlers.toSet() as Set<SagaHandler<*, Saga<*>>>),
        EventDispatcherMiddleware(eventBus.get(), eventStore),
    )

    @DefaultBean
    @Named
    fun provideCommandBus(
        middlewares: Set<CommandMiddleware>,
        handlers: Instance<CommandHandler<*, *>>
    ): CommandBus = CommandBusSynchronous(middlewares, handlers.toSet())

    @DefaultBean
    @Singleton
    @Suppress("UNCHECKED_CAST")
    fun provideEventBus(
        middlewares: Set<EventBusMiddleware>,
        eventCaptors: Instance<EventCaptor<*>>
    ): EventBus = EventBusAsync(middlewares.toSet(), (eventCaptors as Instance<EventCaptor<Event>>).toSet())

    @DefaultBean
    @Named
    fun provideEventBusMiddlewares(): Set<EventBusMiddleware> = setOf(
        EventCaptorLoggerMiddleware()
    )

    @DefaultBean
    @Named
    fun provideQueryBus(
        middlewares: Instance<QueryMiddleware>,
        handlers: Instance<QueryHandler<*, *>>
    ): QueryBus =
        QueryBusSynchronous(middlewares.toSet(), handlers.toSet())
}
