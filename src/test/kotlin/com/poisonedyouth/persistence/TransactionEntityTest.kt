package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class TransactionEntityTest {
    private lateinit var databaseFactory: TestDatabaseFactory

    @BeforeEach
    fun setupDatasource() {
        databaseFactory = TestDatabaseFactory()
        databaseFactory.connect()
    }

    @AfterEach
    fun tearDownDatasource() {
        databaseFactory.close()
    }

    @Test
    fun `creating new transaction is possible`() {
        // given
        val user = transaction {
            UserEntity.new {
                userId = UUID.randomUUID()
                firstName = "John"
                lastName = "Doe"
                birthdate = LocalDate.of(2000, 1, 1)
                password = "passw0rd"
                created = LocalDateTime.of(2022, 1, 1, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 1, 2, 9)
            }
        }

        val account1 = transaction {
            AccountEntity.new {
                name = "My First Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val account2 = transaction {
            AccountEntity.new {
                name = "My Second Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        // when
        val persistedTransaction = transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                originEntity = account1
                targetEntity = account2
                amount = 123.0
                created = LocalDateTime.of(2022, 1, 3, 2, 9)
            }
        }

        // then
        Assertions.assertThat(transaction { TransactionEntity.findById(persistedTransaction.id) }).isNotNull
    }

    @Test
    fun `deleting transaction is possible`() {
        // given
        val user = transaction {
            UserEntity.new {
                userId = UUID.randomUUID()
                firstName = "John"
                lastName = "Doe"
                birthdate = LocalDate.of(2000, 1, 1)
                password = "passw0rd"
                created = LocalDateTime.of(2022, 1, 1, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 1, 2, 9)
            }
        }

        val account1 = transaction {
            AccountEntity.new {
                name = "My First Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val account2 = transaction {
            AccountEntity.new {
                name = "My Second Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val persistedTransaction = transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                originEntity = account1
                targetEntity = account2
                amount = 123.0
                created = LocalDateTime.of(2022, 1, 3, 2, 9)
            }
        }

        // when
        transaction { persistedTransaction.delete() }

        // then
        Assertions.assertThat(transaction { TransactionEntity.findById(persistedTransaction.id) }).isNull()
    }

    @Test
    fun `updating transaction is possible`() {
        // given
        val user = transaction {
            UserEntity.new {
                userId = UUID.randomUUID()
                firstName = "John"
                lastName = "Doe"
                birthdate = LocalDate.of(2000, 1, 1)
                password = "passw0rd"
                created = LocalDateTime.of(2022, 1, 1, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 1, 2, 9)
            }
        }

        val account1 = transaction {
            AccountEntity.new {
                name = "My First Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val account2 = transaction {
            AccountEntity.new {
                name = "My Second Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val persistedTransaction = transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                originEntity = account1
                targetEntity = account2
                amount = 123.0
                created = LocalDateTime.of(2022, 1, 3, 2, 9)
            }
        }

        // when
        transaction { persistedTransaction.amount = 200.0 }

        // then
        Assertions.assertThat(transaction { TransactionEntity.findById(persistedTransaction.id)!!.amount })
            .isEqualTo(200.0)
    }

    @Test
    fun `finding transaction is possible`() {
        // given
        val user = transaction {
            UserEntity.new {
                userId = UUID.randomUUID()
                firstName = "John"
                lastName = "Doe"
                birthdate = LocalDate.of(2000, 1, 1)
                password = "passw0rd"
                created = LocalDateTime.of(2022, 1, 1, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 1, 2, 9)
            }
        }

        val account1 = transaction {
            AccountEntity.new {
                name = "My First Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val account2 = transaction {
            AccountEntity.new {
                name = "My Second Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val persistedTransaction = transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                originEntity = account1
                targetEntity = account2
                amount = 123.0
                created = LocalDateTime.of(2022, 1, 3, 2, 9)
            }
        }

        // when
        val actual = transaction {
            TransactionEntity.find { TransactionTable.transactionId eq persistedTransaction.transactionId }.first()
        }

        // then
        Assertions.assertThat(actual).isNotNull
    }
}
