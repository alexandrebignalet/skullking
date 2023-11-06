package org.skull.king.core.infrastructure

import jakarta.inject.Singleton
import org.skull.king.application.infrastructure.framework.command.CommandBus
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.domain.GameLauncher
import org.skull.king.core.usecases.StartSkullKing

@Singleton
class StartGameFromGameRoom(val commandBus: CommandBus) : GameLauncher {

    override fun startFrom(gameId: String, userIds: Set<String>, configuration: GameConfiguration) {
        StartSkullKing(gameId, userIds.toList(), configuration)
            .let { commandBus.send(it) }
    }
}
