package org.skull.king.core.usecases.captor

import jakarta.inject.Singleton

@Singleton
class ReadSkullKingRepository {
    private val entities = mutableMapOf<String, ReadSkullKing>()

    fun save(skullKing: ReadSkullKing) {
        entities[skullKing.id] = skullKing
    }

    operator fun get(id: String): ReadSkullKing? = entities[id]
}