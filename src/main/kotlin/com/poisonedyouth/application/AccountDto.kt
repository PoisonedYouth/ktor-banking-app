package com.poisonedyouth.application

import java.util.*

data class AccountDto(
    val name: String,
    val accountId: UUID? = null,
    val dispo: Double,
    val limit: Double,
)

data class AccountOverviewDto(
    val name: String,
    val accountId: UUID,
    val balance: Double,
    val dispo: Double,
    val limit: Double,
    val created: String,
    val lastUpdated: String,
)
