package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.core.domain.ClassicConfiguration
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.infrastructure.SkullkingRepository

data class StartSkullKing(
    val gameId: String,
    val players: List<String>,
    val configuration: GameConfiguration = ClassicConfiguration()
) : Command<String>

@ApplicationScoped
class StartHandler(val repository: SkullkingRepository) :
    CommandHandler<StartSkullKing, String> {

    override fun execute(command: StartSkullKing) = repository[command.gameId]
        .start(command.gameId, command.players, command.configuration)
        .let {
            Pair(command.gameId, sequenceOf(it))
        }
}
