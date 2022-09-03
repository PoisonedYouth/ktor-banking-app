package com.poisonedyouth.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
    }
}
