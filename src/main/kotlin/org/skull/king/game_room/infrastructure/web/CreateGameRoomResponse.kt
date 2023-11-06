package org.skull.king.game_room.infrastructure.web

import com.fasterxml.jackson.annotation.JsonCreator

data class CreateGameRoomResponse @JsonCreator constructor(val id: String)
