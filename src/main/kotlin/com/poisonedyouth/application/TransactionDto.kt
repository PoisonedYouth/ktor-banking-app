package com.poisonedyouth.application

import java.util.*

data class TransactionDto(
    val transactionId: UUID? = null,
    val origin: UUID,
    val target: UUID,
    val amount: Double,
)