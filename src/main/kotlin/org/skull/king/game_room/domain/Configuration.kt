package org.skull.king.game_room.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Configuration @JsonCreator constructor(
    @JsonProperty("with_kraken")
    val withKraken: Boolean,
    @JsonProperty("with_whale")
    val withWhale: Boolean,
    @JsonProperty("with_butins")
    val withButins: Boolean
)