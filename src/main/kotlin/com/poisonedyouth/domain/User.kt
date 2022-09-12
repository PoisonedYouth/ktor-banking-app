package com.poisonedyouth.domain

import com.poisonedyouth.security.PasswordManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

private const val MINIMUM_AGE_YEARS = 18L

data class User(
    val userId: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val password: String,
    val created: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    val lastUpdated: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    val accounts: List<Account> = emptyList()
) {
    init {
        val requiredDate = LocalDate.now().minusYears(MINIMUM_AGE_YEARS)
        require(birthdate.isBefore(requiredDate)) {
            "Birthdate must be before '$requiredDate'."
        }
        PasswordManager.validatePassword(password)
    }
}

fun List<Account>.notContainsAccount(vararg accounts: Account) =
    this.find { account -> account.accountId in (accounts.map { it.accountId }) } == null
