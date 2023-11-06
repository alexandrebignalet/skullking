package org.skull.king.application.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.quarkus.jackson.ObjectMapperCustomizer
import jakarta.inject.Singleton

@Singleton
class RegisterCustomModuleCustomizer : ObjectMapperCustomizer {
    override fun customize(mapper: ObjectMapper) {
        mapper.registerKotlinModule()
    }
}