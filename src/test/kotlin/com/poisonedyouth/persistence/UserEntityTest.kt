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

internal class UserEntityTest {

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
    fun `creating new user is possible`() {
        // given + when
        val persistedUser = transaction {
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

        // then
        assertThat(transaction { UserEntity.findById(persistedUser.id) }).isNotNull
    }


    @Test
    fun `deleting user is possible`() {
        // given
        val persistedUser = transaction {
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
        transaction { persistedUser.delete() }

        // then
        assertThat(transaction { UserEntity.findById(persistedUser.id) }).isNull()
    }

    @Test
    fun `edit user is possible`() {
        // given
        val persistedUser = transaction {
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
        transaction { persistedUser.firstName = "Max" }

        // then
        assertThat(transaction { UserEntity.findById(persistedUser.id)!!.firstName }).isEqualTo("Max")
    }

    @Test
    fun `find user is possible`() {
        // given
        transaction {
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
        val actual = transaction { UserEntity.find { UserTable.firstName eq "John" }.first() }

        // then
        assertThat(actual).isNotNull
        assertThat(transaction { actual.accounts.count()}).isZero
    }

    @Test
    fun `find user also loads accounts`() {
        // given
        val user = transaction {
            UserEntity.new {
                userId = UUID.randomUUID()
                firstName = "John"
                lastName = "Doe"
                created = LocalDateTime.of(2022, 1, 1, 1, 9)
                lastUpdated = LocalDateTime.of(2022, 1, 1, 2, 9)
            }
        }

        transaction {
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
        val actual = transaction { UserEntity.find { UserTable.firstName eq "John" }.first() }

        // then
        assertThat(actual).isNotNull
        assertThat(transaction { actual.accounts.count()}).isEqualTo(1)
    }
}
