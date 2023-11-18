package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.RoundFinished

@ApplicationScoped
class NamedBotOnRoundStartedUseCase(val botService: BotService) : EventCaptor<RoundFinished> {

    override fun execute(event: RoundFinished) {
        botService.listen(event)
    }
}