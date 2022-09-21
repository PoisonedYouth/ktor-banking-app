package com.poisonedyouth.api

import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.application.UserDto
import com.poisonedyouth.application.UserPasswordChangeDto
import com.poisonedyouth.application.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.*

class UserController(
    private val userService: UserService
) {
    suspend fun createNewUser(call: ApplicationCall) {
        when (val result = userService.createUser(call.receive())) {
            is Success -> call.respond(HttpStatusCode.Created, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun getExistingUser(call: ApplicationCall) {
        when (val result = userService.findUserByUserId(call.getUserIdFromRequest())) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun updateExistingUser(call: ApplicationCall) {
        val userDto = call.receive<UserDto>()
        when (val result = userService.updateUser(
            userDto.copy(
                userId = UUID.fromString(call.getUserIdFromRequest())
            )
        )) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(
                    httpStatusCode,
                    ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage)
                )
            }
        }
    }

    suspend fun deleteUser(call: ApplicationCall) {
        when (val result = userService.deleteUser(call.getUserIdFromRequest())) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun updatePassword(call: ApplicationCall) {
        val userPasswordChangeDto = call.receive<UserPasswordChangeDto>()
        when (val result = userService.updatePassword(
            userPasswordChangeDto.copy(
                userId = UUID.fromString(call.getUserIdFromRequest())
            )
        )) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun resetUserPassword(call: ApplicationCall) {
        when (val result = userService.resetPassword(call.parameters["userId"])) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun getAllUSer(call: ApplicationCall) {
        when (val result = userService.getAllUser()) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }
}

fun ApplicationCall.getUserIdFromRequest(): String {
    return this.principal<UserIdPrincipal>()?.name ?: "NONE EXISTING USER"
}