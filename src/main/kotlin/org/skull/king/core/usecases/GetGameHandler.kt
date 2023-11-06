package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.query.Query
import org.skull.king.application.infrastructure.framework.query.QueryHandler
import org.skull.king.core.usecases.captor.ReadSkullKing
import org.skull.king.core.usecases.captor.ReadSkullKingRepository

data class GetGame(val gameId: String) : Query<ReadSkullKing>

@ApplicationScoped
class GetGameHandler(val repository: ReadSkullKingRepository) : QueryHandler<GetGame, ReadSkullKing?> {
    override fun execute(command: GetGame) = repository[command.gameId]
}
