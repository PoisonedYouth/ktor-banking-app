package com.poisonedyouth.plugins

import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.application.UserService
import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import org.koin.ktor.ext.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(Application::class.java)

fun Application.configureSecurity() {
    val userService by inject<UserService>()

    authentication {
        basic(name = "userAuthentication") {
            realm = "Ktor Banking App"
            validate { credentials ->
                val result = userService.isValidUser(userId = credentials.name, password = credentials.password)
                if (result is Success && result.value) {
                    UserIdPrincipal(credentials.name)
                } else {
                    logger.error("Authentication failed because of '${(result as Failure).errorCode}' " +
                                     "with message '${result.errorMessage}'")
                    null
                }
            }
        }
    }
}
