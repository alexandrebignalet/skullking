package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.Started

@ApplicationScoped
class BotOnGameStartedUseCase(val botService: BotService) : EventCaptor<Started> {

    override fun execute(event: Started) {
        botService.listen(event)
    }


}