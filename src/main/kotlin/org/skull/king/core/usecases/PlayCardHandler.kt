package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.command.Command
import org.skull.king.application.infrastructure.framework.command.CommandHandler
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.core.domain.Card
import org.skull.king.core.infrastructure.SkullkingRepository

data class PlayCard(val gameId: String, val playerId: String, val card: Card) : Command<String>

@ApplicationScoped
class PlayCardHandler(val repository: SkullkingRepository) :
    CommandHandler<PlayCard, String> {

    override fun execute(command: PlayCard): Pair<String, Sequence<Event>> =
        repository[command.gameId]
            .playCard(command.playerId, command.card)
            .let {
                Pair(command.gameId, sequenceOf(it))
            }
}
