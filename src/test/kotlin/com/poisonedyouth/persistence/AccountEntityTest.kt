package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class AccountEntityTest {

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
    fun `creating new account is possible`() {
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

        // when
        val persistedAccount = transaction {
            AccountEntity.new {
                name = "My Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        // then
        assertThat(transaction { AccountEntity.findById(persistedAccount.id) }).isNotNull
    }

    @Test
    fun `deleting account is possible`() {
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

        val persistedAccount = transaction {
            AccountEntity.new {
                name = "My Account"
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
        transaction { persistedAccount.delete() }

        // then
        assertThat(transaction { AccountEntity.findById(persistedAccount.id) }).isNull()
        assertThat(transaction { UserEntity.findById(user.id) }).isNotNull
    }

    @Test
    fun `updating account is possible`() {
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

        val persistedAccount = transaction {
            AccountEntity.new {
                name = "My Account"
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
        transaction { persistedAccount.balance = 333.0 }

        // then
        assertThat(transaction { AccountEntity.findById(persistedAccount.id)!!.balance }).isEqualTo(333.0)
    }

    @Test
    fun `delete user also set relation to account to null`() {
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

        val persistedAccount = transaction {
            AccountEntity.new {
                name = "My Account"
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
        transaction { user.delete()}

        // then
        assertThat(transaction { AccountEntity.findById(persistedAccount.id)}).isNotNull
    }

    @Test
    fun `find account is possible`() {
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

        val persistedAccount = transaction {
            AccountEntity.new {
                name = "My Account"
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
        val actual = transaction { AccountEntity.find { AccountTable.accountId eq persistedAccount.accountId } }

        // then
        assertThat(actual).isNotNull
    }

    @Test
    fun `find account also loads origin transactions`() {
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

        val account = transaction {
            AccountEntity.new {
                name = "My Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val otherAccount = transaction {
            AccountEntity.new {
                name = "Other Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                amount = 100.0
                originEntity = account
                targetEntity = otherAccount
                created = LocalDateTime.of(2022, 2, 2, 2, 9)
            }
        }

        // when
        val actual = transaction { AccountEntity.find { AccountTable.accountId eq account.accountId }.first() }

        // then
        assertThat(transaction { actual.originTransactions.count() }).isEqualTo(1)
        assertThat(transaction { actual.targetTransactions.count() }).isZero
    }

    @Test
    fun `find account also loads target transactions`() {
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

        val account = transaction {
            AccountEntity.new {
                name = "My Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        val otherAccount = transaction {
            AccountEntity.new {
                name = "Other Account"
                accountId = UUID.randomUUID()
                balance = 120.0
                dispo = -100.0
                limit = 100.0
                created = LocalDateTime.of(2022, 1, 2, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 2, 2, 9)
                userEntity = user
            }
        }

        transaction {
            TransactionEntity.new {
                transactionId = UUID.randomUUID()
                amount = 100.0
                originEntity = otherAccount
                targetEntity = account
                created = LocalDateTime.of(2022, 2, 2, 2, 9)
            }
        }

        // when
        val actual = transaction { AccountEntity.find { AccountTable.accountId eq account.accountId }.first() }

        // then
        assertThat(transaction { actual.targetTransactions.count() }).isEqualTo(1)
        assertThat(transaction { actual.originTransactions.count() }).isZero
    }
}
