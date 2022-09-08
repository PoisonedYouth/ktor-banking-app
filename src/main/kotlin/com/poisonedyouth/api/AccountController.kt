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
            accountService.findByUserIdAndAccountId(userId = call.parameters["userId"], call.parameters["accountId"])) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }
}