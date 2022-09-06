package com.poisonedyouth.api

import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ErrorCode
import io.ktor.http.HttpStatusCode

internal fun getHttpStatusCodeFromErrorCode(result: Failure) = when (result.errorCode) {
    ErrorCode.USER_NOT_FOUND,
    ErrorCode.TRANSACTION_NOT_FOUND,
    ErrorCode.ACCOUNT_NOT_FOUND -> HttpStatusCode.NotFound

    ErrorCode.PASSWORD_ERROR,
    ErrorCode.TRANSACTION_REQUEST_INVALID,
    ErrorCode.MAPPING_ERROR -> HttpStatusCode.BadRequest

    ErrorCode.NOT_ALLOWED -> HttpStatusCode.Forbidden
    ErrorCode.ACCOUNT_ALREADY_EXIST,
    ErrorCode.USER_ALREADY_EXIST -> HttpStatusCode.Conflict

    ErrorCode.DATABASE_ERROR -> HttpStatusCode.InternalServerError
}

data class SuccessDto<T>(
    val value: T
)

data class ErrorDto(
    val errorCode: ErrorCode,
    val errorMessage: String
)