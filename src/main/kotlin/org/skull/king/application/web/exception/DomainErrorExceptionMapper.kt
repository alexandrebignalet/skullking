package org.skull.king.application.web.exception

import jakarta.annotation.Priority
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.skull.king.core.domain.*


@Provider
@Priority(1)
class DomainErrorExceptionMapper : ExceptionMapper<DomainError> {
    init {
        println("registered")
    }

    override fun toResponse(exception: DomainError): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(BaseErrorMessage(exception.message, DomainErrorCode.resolve(exception).name))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}

enum class DomainErrorCode {
    INTERNAL,
    ALREADY_ANNOUNCED,
    OVER,
    CARD_NOT_ALLOWED,
    NOT_YOUR_TURN,
    ALL_MUST_ANNOUNCE;

    companion object {
        fun resolve(error: DomainError) = when (error) {
            is PlayerAlreadyAnnouncedError -> ALREADY_ANNOUNCED
            is SkullKingOverError -> OVER
            is CardNotAllowedError -> CARD_NOT_ALLOWED
            is NotYourTurnError -> NOT_YOUR_TURN
            is SkullKingNotReadyError -> ALL_MUST_ANNOUNCE
            else -> INTERNAL
        }
    }
}

