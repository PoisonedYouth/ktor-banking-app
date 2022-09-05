package com.poisonedyouth.persistence

import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction

    fun findAllByAccount(account: Account): List<Transaction>

}

class TransactionRepositoryImpl : TransactionRepository {
    override fun save(transaction: Transaction): Transaction = transaction {
        val currentDateTime = LocalDateTime.now()
        val existingTransaction =
            TransactionEntity.find { TransactionTable.transactionId eq transaction.transactionId }.firstOrNull()
        if (existingTransaction == null) {
            val transactionEntity = TransactionEntity.new {
                transactionId = transaction.transactionId
                originEntity =
                    AccountEntity.find { AccountTable.accountId eq transaction.origin.accountId }.firstOrNull()
                        ?: error("Account '${transaction.origin.accountId}' not available in database!")
                targetEntity =
                    AccountEntity.find { AccountTable.accountId eq transaction.target.accountId }.firstOrNull()
                        ?: error("Account '${transaction.target.accountId}' not available in database!")
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

    override fun findAllByAccount(account: Account): List<Transaction> = transaction {
        TransactionEntity.find { TransactionTable.origin eq account.accountId or (TransactionTable.target eq account.accountId) }
            .map { it.toTransaction() }
    }
}


fun TransactionEntity.toTransaction() = Transaction(
    transactionId = this.transactionId,
    origin = this.originEntity.toAccount(),
    target = this.targetEntity.toAccount(),
    amount = this.amount,
    created = this.created
)
