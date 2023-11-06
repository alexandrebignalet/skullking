package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.NewRoundStarted

@ApplicationScoped
class NamedBotOnRoundStartedUseCase(val botService: BotService) : EventCaptor<NewRoundStarted> {

    override fun execute(event: NewRoundStarted) {
        botService.listen(event)
    }
}