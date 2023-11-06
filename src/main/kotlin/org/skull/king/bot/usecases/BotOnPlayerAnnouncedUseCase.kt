package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.PlayerAnnounced

@ApplicationScoped
class BotOnPlayerAnnouncedUseCase(val botService: BotService) : EventCaptor<PlayerAnnounced> {

    override fun execute(event: PlayerAnnounced) {
        botService.listen(event)
    }
}