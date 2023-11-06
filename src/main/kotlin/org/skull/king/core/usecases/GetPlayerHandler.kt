package org.skull.king.core.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.skull.king.application.infrastructure.framework.query.Query
import org.skull.king.application.infrastructure.framework.query.QueryHandler
import org.skull.king.core.usecases.captor.ReadPlayer
import org.skull.king.core.usecases.captor.ReadSkullKingRepository

data class GetPlayer(val gameId: String, val playerId: String) : Query<ReadPlayer>

@ApplicationScoped
class GetPlayerHandler(val repository: ReadSkullKingRepository) : QueryHandler<GetPlayer, ReadPlayer?> {

    override fun execute(command: GetPlayer) = repository[command.gameId]?.players?.find { it.id == command.playerId }
}
