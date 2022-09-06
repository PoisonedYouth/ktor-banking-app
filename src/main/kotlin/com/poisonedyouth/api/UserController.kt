package com.poisonedyouth.api

import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.application.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class UserController(
    private val userService: UserService
) {
    suspend fun createNewUser(call: ApplicationCall) {
        when (val result = userService.createUser(call.receive())) {
            is Success -> call.respond(HttpStatusCode.Created, result.value)
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, result.errorMessage)
            }
        }
    }

    suspend fun getExistingUser(call: ApplicationCall) {
        when (val result = userService.findUserByUserId(call.parameters["userId"])) {
            is Success -> call.respond(HttpStatusCode.OK, result.value)
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, result.errorMessage)
            }
        }
    }

    suspend fun updateExistingUser(call: ApplicationCall) {
        when (val result = userService.updateUser(call.receive())) {
            is Success -> call.respond(HttpStatusCode.OK, result.value)
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, result.errorMessage)
            }
        }
    }

    suspend fun deleteUser(call: ApplicationCall) {
        when (val result = userService.deleteUser(call.parameters["userId"])) {
            is Success -> call.respond(HttpStatusCode.OK, result.value)
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, result.errorMessage)
            }
        }
    }
}