package com.poisonedyouth.domain

import java.util.UUID

data class Administrator(
    val adminId: UUID = UUID.randomUUID(),
    val name: String,
    val password: String,
)
