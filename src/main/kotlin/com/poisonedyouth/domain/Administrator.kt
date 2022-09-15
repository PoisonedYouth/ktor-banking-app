package com.poisonedyouth.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Administrator(
    val administratorId: UUID = UUID.randomUUID(),
    val name: String,
    val password: String,
    val created: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    val lastUpdated: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
)
