package com.poisonedyouth.api

import com.poisonedyouth.application.AdministratorService
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.application.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.util.decodeBase64String

class AuthenticationController(
    private val administratorService: AdministratorService
) {

    suspend fun checkAdministratorLogin(call: ApplicationCall) {
        when (val result =
            administratorService.isValidAdministrator(
                administratorId = call.request.queryParameters["administratorId"]?.decodeBase64String(),
                password = call.request.queryParameters["password"]?.decodeBase64String()
            )) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }
}
