package com.poisonedyouth.application

import java.time.LocalDateTime
import java.util.*

data class AccountOverviewDto(
    val name: String,
    val accountId: UUID,
    val balance: Double,
    val dispo: Double,
    val limit: Double,
    val created: String,
    val lastUpdated: String,
)
