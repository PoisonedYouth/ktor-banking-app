package com.poisonedyouth.api

import com.poisonedyouth.application.AccountService
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class AccountController(
    private val accountService: AccountService
) {
    suspend fun getExistingAccount(call: ApplicationCall) {
        when (val result =
            accountService.findByUserIdAndAccountId(
                userId = call.getUserIdFromRequest(),
                call.parameters["accountId"]
            )) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun createNewAccount(call: ApplicationCall) {
        when (val result =
            accountService.createAccount(userId = call.getUserIdFromRequest(), call.receive())) {
            is Success -> call.respond(HttpStatusCode.Created, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(
                    httpStatusCode,
                    ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage)
                )
            }
        }
    }

    suspend fun updateExistingAccount(call: ApplicationCall) {
        when (val result =
            accountService.updateAccount(userId = call.getUserIdFromRequest(), call.receive())) {
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

    suspend fun deleteAccount(call: ApplicationCall) {
        when (val result =
            accountService.deleteAccount(userId = call.getUserIdFromRequest(), call.parameters["accountId"])) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }
}