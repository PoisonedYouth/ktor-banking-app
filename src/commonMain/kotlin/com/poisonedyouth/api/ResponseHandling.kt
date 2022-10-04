package com.poisonedyouth.api

import com.poisonedyouth.api.ApiResult.Failure

internal fun getHttpStatusCodeFromErrorCode(result: Failure) = when (result.errorCode) {
    ErrorCode.USER_NOT_FOUND,
    ErrorCode.TRANSACTION_NOT_FOUND,
    ErrorCode.ADMINISTRATOR_NOT_FOUND,
    ErrorCode.ACCOUNT_NOT_FOUND -> 404 // Not found

    ErrorCode.PASSWORD_ERROR,
    ErrorCode.TRANSACTION_REQUEST_INVALID,
    ErrorCode.MAPPING_ERROR -> 400 // Bad Request

    ErrorCode.NOT_ALLOWED ->403 // Forbidden
    ErrorCode.ACCOUNT_ALREADY_EXIST,
    ErrorCode.USER_ALREADY_EXIST -> 409 // Conflict

    ErrorCode.DATABASE_ERROR -> 500 // InternalServerError
}

data class SuccessDto<T>(
    val value: T
)

data class ErrorDto(
    val errorCode: ErrorCode,
    val errorMessage: String
)