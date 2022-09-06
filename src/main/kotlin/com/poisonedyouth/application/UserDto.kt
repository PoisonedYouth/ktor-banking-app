package com.poisonedyouth.application

import java.util.*

data class UserDto(
    val userId: UUID? = null,
    val firstName: String,
    val lastName: String,
    val birthdate: String,
    val password: String
)
data class UserOverviewDto(
    val userId: UUID? = null,
    val firstName: String,
    val lastName: String,
    val birthdate: String,
    val password: String,
    val created: String,
    val lastUpdated: String,
    val account: List<AccountOverviewDto>
)

data class UserPasswordChangeDto(
    val userId: UUID,
    val existingPassword: String,
    val newPassword: String
)