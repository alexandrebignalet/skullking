package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.core.infrastructure.SkullkingRepository

data class SettleFoldWinner(val gameId: String) : Command<String>

@ApplicationScoped
class SettleFoldHandler(val repository: SkullkingRepository) :
    CommandHandler<SettleFoldWinner, String> {

    override fun execute(command: SettleFoldWinner) =
        repository[command.gameId]
            .settleFold()
            .let { Pair(command.gameId, it) }
}
