package com.poisonedyouth.plugins

import com.poisonedyouth.api.AccountController
import com.poisonedyouth.api.UserController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userController by inject<UserController>()
    val accountController by inject<AccountController>()

    routing {
        route("/api/user") {
            get("/{userId}") {
                userController.getExistingUser(call)
            }
            post("") {
                userController.createNewUser(call)
            }
            put("") {
                userController.updateExistingUser(call)
            }
            delete("/{userId}") {
                userController.deleteUser(call)
            }
            put("/{userId}/password") {
                userController.updatePassword(call)
            }
        }
        route("/api/user/{userId}/account") {
            get("/{accountId}") {
                accountController.getExistingAccount(call)
            }
            post("") {

            }
            put("") {

            }
            delete("/{accountId}") {

            }
        }
    }
}