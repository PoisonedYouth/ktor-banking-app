package com.poisonedyouth.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

data class Transaction(
    val transactionId: UUID = UUID.randomUUID(),
    val origin: Account,
    val target: Account,
    val amount: Double,
    val created: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
) {
    init {
        require(amount > 0.0) {
            "Amount must be positive."
        }
    }
}
