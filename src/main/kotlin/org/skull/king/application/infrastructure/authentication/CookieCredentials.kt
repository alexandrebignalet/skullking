package org.skull.king.application.infrastructure.authentication

data class CookieCredentials(val value: String) {
    fun toUser() = value.split(":").let { User(it.first(), it.last()) }
}
