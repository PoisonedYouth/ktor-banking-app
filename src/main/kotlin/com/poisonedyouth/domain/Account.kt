package com.poisonedyouth.domain

import java.time.LocalDateTime
import java.util.*

data class Account(
    val name: String,
    val accountId: UUID = UUID.randomUUID(),
    val balance: Double,
    val dispo: Double,
    val limit: Double,
    val created: LocalDateTime = LocalDateTime.now(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val user: User
)
