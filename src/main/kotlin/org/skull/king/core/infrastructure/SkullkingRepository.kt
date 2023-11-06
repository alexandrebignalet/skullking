package org.skull.king.core.infrastructure

import jakarta.inject.Singleton
import org.skull.king.application.infrastructure.framework.ddd.event.EventStore
import org.skull.king.application.infrastructure.framework.infrastructure.persistence.EventSourcedRepository
import org.skull.king.core.domain.state.SkullKingEvent
import org.skull.king.core.domain.state.Skullking
import org.skull.king.core.domain.state.StartState

@Singleton
class SkullkingRepository(
    eventStore: EventStore
) : EventSourcedRepository<String, SkullKingEvent, Skullking>(eventStore) {

    override fun load(id: String) = eventStore.allOf(id, Skullking::class.java).consume {
        it.fold(StartState) { i: Skullking, e -> i.compose(e as SkullKingEvent, it.count()) }
    }

    override fun exists(id: String) = load(id) != StartState

    override fun add(events: Sequence<SkullKingEvent>) {}

    override fun delete(racine: Skullking) {}
}
