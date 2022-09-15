package com.poisonedyouth.plugins

import com.poisonedyouth.application.AdministratorService
import com.poisonedyouth.application.ApiResult
import com.poisonedyouth.application.ErrorCode
import com.poisonedyouth.application.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import io.ktor.server.auth.basicAuthenticationCredentials
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(Application::class.java)

fun Application.configureSecurity() {
    val userService by inject<UserService>()
    val administratorService by inject<AdministratorService>()

    install(StatusPages) {
        status(HttpStatusCode.Unauthorized) { call, _ ->
            if (call.request.path().startsWith("/api/user")) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResult.Failure(
                        ErrorCode.USER_NOT_FOUND,
                        "Authentication for user with userId '${call.request.basicAuthenticationCredentials()?.name}' failed."
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResult.Failure(
                        ErrorCode.ADMINISTRATOR_NOT_FOUND,
                        "Authentication for administrator with administratorId '${call.request.basicAuthenticationCredentials()?.name}' failed."
                    )
                )
            }
        }
    }

    authentication {
        basic(name = "userAuthentication") {
            realm = "Ktor Banking App"
            validate { credentials ->
                val result = userService.isValidUser(userId = credentials.name, password = credentials.password)
                if (result is ApiResult.Success && result.value) {
                    UserIdPrincipal(credentials.name)
                } else {
                    logger.error(
                        "Authentication failed because of '${(result as ApiResult.Failure).errorCode}' " +
                            "with message '${result.errorMessage}'"
                    )
                    null
                }
            }
        }

        basic(name = "administratorAuthentication") {
            realm = "Ktor Banking App"
            validate { credentials ->
                val result = administratorService.isValidAdministrator(
                    administratorId = credentials.name,
                    password = credentials.password
                )
                if (result is ApiResult.Success && result.value) {
                    UserIdPrincipal(credentials.name)
                } else {
                    logger.error(
                        "Authentication failed because of '${(result as ApiResult.Failure).errorCode}' " +
                            "with message '${result.errorMessage}'"
                    )
                    null
                }
            }
        }
    }
}
