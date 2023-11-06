package org.skull.king.bot.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.ddd.event.EventCaptor
import org.skull.king.bot.infrastructure.BotService
import org.skull.king.core.domain.state.FoldSettled

@ApplicationScoped
class BotOnFoldSettledUseCase(val botService: BotService) : EventCaptor<FoldSettled> {

    override fun execute(event: FoldSettled) {
        botService.listen(event)
    }
}

