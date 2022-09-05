package com.poisonedyouth.application

import java.util.UUID

data class UserDto(
    val userId: UUID? = null,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val password: String
)