package com.poisonedyouth.persistence

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

interface AccountRepository {

    fun saveForUser(user: User, account: Account): Account

    fun delete(account: Account)
}

class AccountRepositoryImpl : AccountRepository {

    override fun saveForUser(user: User, account: Account) = transaction {
        val existingUser = UserEntity.find { UserTable.userId eq user.userId }.firstOrNull()
            ?: error("User '${user.userId}' not persisted yet!")
        val currentDateTime = LocalDateTime.now()
        val existingAccount = AccountEntity.find { AccountTable.accountId eq account.accountId  }.singleOrNull()
        if (existingAccount == null) {
            val accountEntity = AccountEntity.new {
                accountId = account.accountId
                name = account.name
                balance = account.balance
                dispo = account.dispo
                limit = account.limit
                created = currentDateTime
                lastUpdated = currentDateTime
                userEntity = existingUser

            }
            account.copy(
                created = currentDateTime,
                lastUpdated = currentDateTime
            )
        } else {
            existingAccount.accountId = account.accountId
            existingAccount.name = account.name
            existingAccount.balance = account.balance
            existingAccount.dispo = account.dispo
            existingAccount.limit = account.limit
            existingAccount.lastUpdated = currentDateTime
            account.copy(
                lastUpdated = currentDateTime
            )
        }
    }

    override fun delete(account: Account) = transaction {
        AccountEntity.find { AccountTable.accountId eq account.accountId }.firstOrNull().let {
            if (it == null) {
                error("Account '${account.accountId}' does not exist!")
            } else {
                it.userEntity = null
            }
        }
    }
}

fun AccountEntity.toAccount() = Account(
    name = this.name,
    accountId = this.accountId,
    balance = this.balance,
    dispo = this.dispo,
    limit = this.limit,
    created = this.created,
    lastUpdated = this.lastUpdated
)
