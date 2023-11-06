package org.skull.king.application.infrastructure.authentication

import java.security.Principal

data class User(val id: String, val userName: String) : Principal {
    override fun getName() = "$id:$userName"
}
