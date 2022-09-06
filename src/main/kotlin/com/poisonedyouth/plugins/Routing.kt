package com.poisonedyouth.plugins

import com.poisonedyouth.api.UserController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userController by inject<UserController>()

    routing() {
        get("/api/user/{userId}") {

        }
        post("/api/user") {
            userController.createNewUser(call)
        }
        put("/api/user") {

        }
        delete("/api/user/{userId}") {

        }
        put("/api/user/{userId}/password") {

        }
    }
}