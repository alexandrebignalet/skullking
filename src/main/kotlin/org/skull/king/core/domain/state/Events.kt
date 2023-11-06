package org.skull.king.core.domain.state

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.skull.king.application.infrastructure.framework.ddd.event.Event
import org.skull.king.core.domain.Card
import org.skull.king.core.domain.GameConfiguration
import org.skull.king.core.domain.NewPlayer
import org.skull.king.core.domain.PlayerId
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Started::class, name = Started.EVENT_TYPE),
    JsonSubTypes.Type(value = PlayerAnnounced::class, name = PlayerAnnounced.EVENT_TYPE),
    JsonSubTypes.Type(value = CardPlayed::class, name = CardPlayed.EVENT_TYPE),
    JsonSubTypes.Type(value = FoldSettled::class, name = FoldSettled.EVENT_TYPE),
    JsonSubTypes.Type(value = NewRoundStarted::class, name = NewRoundStarted.EVENT_TYPE),
    JsonSubTypes.Type(value = GameFinished::class, name = GameFinished.EVENT_TYPE)
)
sealed class SkullKingEvent(
    override val aggregateId: String,
    override val type: String,
    override val version: Int = 0,
    override val aggregateType: String = SKULLKING_AGGREGATE_TYPE,
    override val timestamp: Long = Instant.now().toEpochMilli()
) : Event {
    companion object {
        const val SKULLKING_AGGREGATE_TYPE = "SKULLKING"
    }
}

data class Started(val gameId: String, val players: List<NewPlayer>, val configuration: GameConfiguration) :
    SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "game_started"
    }
}

data class PlayerAnnounced(
    val gameId: String,
    val playerId: String,
    val announce: Int,
    val roundNb: Int,
    val isLast: Boolean,
    override val version: Int = 1
) : SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "player_announced"
    }
}

data class CardPlayed(
    val gameId: String,
    val playerId: String,
    val card: Card,
    val isLastFoldPlay: Boolean = false,
    override val version: Int
) : SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "card_played"
    }
}

data class FoldSettled(
    val gameId: String,
    val winnerPlayerId: PlayerId,
    val bonus: Int,
    val won: Boolean,
    val butinAllies: List<String>,
    override val version: Int,
) :
    SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "fold_settled"
    }
}

data class NewRoundStarted(
    val gameId: String,
    val roundNb: Int,
    val players: List<NewPlayer>,
    override val version: Int
) :
    SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "new_round_finished"
    }
}

data class GameFinished(val gameId: String, override val version: Int) : SkullKingEvent(gameId, EVENT_TYPE) {
    companion object {
        const val EVENT_TYPE = "game_finished"
    }
}
