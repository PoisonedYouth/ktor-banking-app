package com.poisonedyouth.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class User(
    val userId: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val password: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val accounts: List<Account> = emptyList()
)
