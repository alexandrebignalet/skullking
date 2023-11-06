package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.CardPlayed

@ApplicationScoped
class BotOnCardPlayedUseCase(val botService: BotService) : EventCaptor<CardPlayed> {

    override fun execute(event: CardPlayed) {
        botService.listen(event)
    }
}