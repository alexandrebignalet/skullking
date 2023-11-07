package org.skull.king.application.infrastructure.authentication

import io.quarkus.qute.TemplateData
import java.security.Principal

@TemplateData
data class User(val id: String, val userName: String) : Principal {
    override fun getName() = "$id:$userName"
}
