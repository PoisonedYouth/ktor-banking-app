package com.poisonedyouth.domain

import java.time.LocalDateTime
import java.util.*

data class Account(
    val name: String,
    val accountId: UUID = UUID.randomUUID(),
    val balance: Double = 0.0,
    val dispo: Double,
    val limit: Double,
    val created: LocalDateTime = LocalDateTime.now(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
){
    init {
        require(limit > 0){
            "Limit must be positive."
        }
    }
}
