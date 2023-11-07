package org.skull.king.application.infrastructure.authentication

data class CookieCredentials(val value: String) {
    fun toUser(): User = value
        .replace("$AUTH_COOKIE_NAME=", "")
        .split(":").let { User(it.first(), it.last()) }
}
