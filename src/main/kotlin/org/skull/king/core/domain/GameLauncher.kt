package org.skull.king.core.domain

interface GameLauncher {
    fun startFrom(gameId: String, userIds: Set<String>, configuration: GameConfiguration)
}
