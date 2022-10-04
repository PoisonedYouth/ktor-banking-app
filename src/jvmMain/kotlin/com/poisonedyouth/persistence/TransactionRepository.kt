package com.poisonedyouth.persistence

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction
    fun findAllByAccount(account: Account): List<Transaction>
    fun findByTransactionId(transactionId: UUID): Transaction?
    fun delete(transaction: Transaction)
    fun findAll(): List<Transaction>
}

class TransactionRepositoryImpl : TransactionRepository {
    override fun save(transaction: Transaction): Transaction = transaction {
        val currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val existingTransaction =
            TransactionEntity.find { TransactionTable.transactionId eq transaction.transactionId }.firstOrNull()
        if (existingTransaction == null) {
            TransactionEntity.new {
                transactionId = transaction.transactionId
                originEntity = findAccountBy(transaction.origin.accountId)
                targetEntity = findAccountBy(transaction.target.accountId)
                amount = transaction.amount
                created = LocalDateTime.now()
            }
            transaction.copy(
                created = currentDateTime,
            )
        } else {
            error("Transactions cannot be updated!")
        }
    }

    private fun findAccountBy(accountId: UUID) =
        (AccountEntity.find { AccountTable.accountId eq accountId }.firstOrNull()
            ?: error("Account with accountId '${accountId}' not available in database!"))

    override fun findAllByAccount(account: Account): List<Transaction> = transaction {
        TransactionEntity.find { TransactionTable.origin eq account.accountId or (TransactionTable.target eq account.accountId) }
            .map { it.toTransaction() }
    }

    override fun findByTransactionId(transactionId: UUID): Transaction? = transaction {
        TransactionEntity.find { TransactionTable.transactionId eq transactionId }.firstOrNull()?.toTransaction()
    }

    override fun delete(transaction: Transaction) = transaction {
        val existingTransaction =
            TransactionEntity.find { TransactionTable.transactionId eq transaction.transactionId }.firstOrNull()
                ?: error("Transaction with transactionId '${transaction.transactionId}' does not exist in database.")
        existingTransaction.delete()
    }

    override fun findAll(): List<Transaction> = transaction {
        TransactionEntity.all().map { it.toTransaction() }
    }
}


fun TransactionEntity.toTransaction() = Transaction(
    transactionId = this.transactionId,
    origin = this.originEntity.toAccount(),
    target = this.targetEntity.toAccount(),
    amount = this.amount,
    created = this.created.truncatedTo(ChronoUnit.SECONDS)
)
