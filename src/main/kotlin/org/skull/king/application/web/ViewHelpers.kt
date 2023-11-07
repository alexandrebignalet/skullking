package org.skull.king.application.web

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder
import java.net.URI

fun redirectToGameRoom(error: String? = null): Response {
    val builder = UriBuilder.fromUri(URI("/skullking/game_rooms"))
    error?.let { builder.queryParam("error", error) }

    return Response
        .status(302)
        .location(builder.build())
        .build()
}

fun redirectToGame(gameId: String): Response {
    return Response
        .status(302)
        .location(URI("/skullking/games/$gameId"))
        .build()
}