package org.skull.king.game_room.infrastructure.web

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.skull.king.application.web.exception.BaseErrorMessage
import org.skull.king.game_room.domain.BaseGameRoomException

@Provider
class GameRoomExceptionMapper : ExceptionMapper<BaseGameRoomException> {
    override fun toResponse(exception: BaseGameRoomException): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(BaseErrorMessage(exception.message))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}
