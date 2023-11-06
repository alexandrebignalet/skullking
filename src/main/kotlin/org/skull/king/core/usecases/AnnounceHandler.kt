package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.core.domain.PlayerAlreadyAnnouncedError
import org.skull.king.core.domain.SkullKingNotStartedError
import org.skull.king.core.domain.SkullKingOverError
import org.skull.king.core.domain.state.AnnounceState
import org.skull.king.core.domain.state.OverState
import org.skull.king.core.domain.state.RoundState
import org.skull.king.core.domain.state.StartState
import org.skull.king.core.infrastructure.SkullkingRepository

data class AnnounceWinningCardsFoldCount(val gameId: String, val playerId: String, val count: Int) : Command<String>

@ApplicationScoped
class AnnounceHandler(val repository: SkullkingRepository) :
    CommandHandler<AnnounceWinningCardsFoldCount, String> {

    override fun execute(command: AnnounceWinningCardsFoldCount): Pair<String, Sequence<Event>> {
        return when (val game = repository[command.gameId]) {
            is StartState -> throw SkullKingNotStartedError(game)
            is AnnounceState -> game.announce(command.playerId, command.count)
            is RoundState -> throw PlayerAlreadyAnnouncedError("Player ${command.playerId} already announced")
            is OverState -> throw SkullKingOverError(game)
        }
    }
}
