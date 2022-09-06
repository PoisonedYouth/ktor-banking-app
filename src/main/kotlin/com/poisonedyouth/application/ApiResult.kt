package com.poisonedyouth.application

sealed class ApiResult<out T> {
    internal data class Failure(val errorCode: ErrorCode, val errorMessage: String) : ApiResult<Nothing>()
    internal data class Success<T>(val value: T) : ApiResult<T>()
}

enum class ErrorCode {
    DATABASE_ERROR,
    MAPPING_ERROR,
    USER_NOT_FOUND,
    USER_ALREADY_EXIST,
    ACCOUNT_NOT_FOUND,
    ACCOUNT_ALREADY_EXIST,
    PASSWORD_ERROR,
    NOT_ALLOWED,
    TRANSACTION_REQUEST_INVALID,
    TRANSACTION_NOT_FOUND
}