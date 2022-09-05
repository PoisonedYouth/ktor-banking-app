package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDate
import java.util.*

class AccountRepositoryTest : KoinTest {

    private lateinit var databaseFactory: TestDatabaseFactory
    private val accountRepository by inject<AccountRepository>()
    private val userRepository by inject<UserRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            bankingAppModule
        )
    }

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
    fun `save persists new account to database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "passw0rd",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        // when
        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val actual = accountRepository.saveForUser(persistedUser, account)

        // then
        assertThat(actual).isNotNull
        assertThat(transaction { AccountEntity.find { AccountTable.accountId eq actual.accountId }.single()}).isNotNull
    }

    @Test
    fun `save throws exception if user is not available in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "passw0rd",
            accounts = listOf()
        )

        // when + then
        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        assertThatThrownBy { accountRepository.saveForUser(user, account) }.isInstanceOf(
            IllegalStateException::class.java
        )
    }

    @Test
    fun `save updates existing account to database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "passw0rd",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)

        // when
        val updatedAccount = accountRepository.saveForUser(
            persistedUser, persistedAccount.copy(
                name = "My other account"
            )
        )

        // then
        assertThat(updatedAccount).isNotNull
        assertThat(transaction { AccountEntity.all().count() }).isEqualTo(1)
        assertThat(transaction {
            AccountEntity.find { AccountTable.accountId eq updatedAccount.accountId }.single().name
        })
            .isEqualTo("My other account")
    }

    @Test
    fun `delete removes account user relation from database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "passw0rd",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)

        // when
        accountRepository.delete(persistedAccount)

        // then
        assertThat(transaction { AccountEntity.all().count() }).isEqualTo(1)
        assertThat(transaction {
            AccountEntity.find { AccountTable.accountId eq persistedAccount.accountId }.single().userEntity
        }).isNull()
    }

    @Test
    fun `delete throws exception if account not exists in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "passw0rd",
            accounts = listOf()
        )
        userRepository.save(user)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )

        // when + then
        assertThatThrownBy {
            accountRepository.delete(account)
        }.isInstanceOf(IllegalStateException::class.java)
    }
}
