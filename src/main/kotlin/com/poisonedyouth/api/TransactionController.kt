package com.poisonedyouth.api

import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.application.TransactionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class TransactionController(
    private val transactionService: TransactionService
) {

    suspend fun getExistingTransaction(call: ApplicationCall) {
        when (val result =
            transactionService.getTransaction(userId = call.getUserIdFromRequest(), call.parameters["transactionId"])) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun createNewTransaction(call: ApplicationCall) {
        when (val result =
            transactionService.createTransaction(userId = call.getUserIdFromRequest(), call.receive())) {
            is Success -> call.respond(HttpStatusCode.Created, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun deleteTransaction(call: ApplicationCall) {
        when (val result =
            transactionService.deleteTransaction(transactionId = call.parameters["transactionId"])) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }

    suspend fun getAllExistingTransaction(call: ApplicationCall) {
        when (val result =
            transactionService.getAllTransactions()) {
            is Success -> call.respond(HttpStatusCode.OK, SuccessDto(result.value))
            is Failure -> {
                val httpStatusCode = getHttpStatusCodeFromErrorCode(result)
                call.respond(httpStatusCode, ErrorDto(errorCode = result.errorCode, errorMessage = result.errorMessage))
            }
        }
    }
}