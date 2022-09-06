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
            password = "Ta1&tudol3lal54e",
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
        assertThat(transaction { AccountEntity.find { AccountTable.accountId eq actual.accountId }.single() }).isNotNull
    }

    @Test
    fun `save throws exception if user is not available in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
    fun `save fails if account exist in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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

        // when + then
        assertThatThrownBy {
            accountRepository.saveForUser(
                persistedUser, persistedAccount
            )
        }.isInstanceOf(
            IllegalStateException::class.java
        )
    }

    @Test
    fun `update throws exception if user is not available in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        assertThatThrownBy { accountRepository.updateForUser(user, account) }.isInstanceOf(
            IllegalStateException::class.java
        )
    }

    @Test
    fun `update throws exception if account is not available in database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedUser = userRepository.save(user)

        // when + then
        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        assertThatThrownBy { accountRepository.updateForUser(persistedUser, account) }.isInstanceOf(
            IllegalStateException::class.java
        )
    }

    @Test
    fun `update persists changes of account to database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        accountRepository.saveForUser(persistedUser, account)


        // when
        val actual = accountRepository.updateForUser(
            persistedUser, account.copy(
                name = "Other Account"
            )
        )

        // then
        assertThat(actual).isNotNull
        assertThat(transaction {
            AccountEntity.find { AccountTable.accountId eq actual.accountId }.single().name
        }).isEqualTo("Other Account")
    }

    @Test
    fun `delete removes account user relation from database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
            password = "Ta1&tudol3lal54e",
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

    @Test
    fun `findByAccountId returns matching account`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        val actual = accountRepository.findByAccountId(persistedAccount.accountId)

        // then
        assertThat(actual).isEqualTo(persistedAccount)
    }

    @Test
    fun `findByAccountId returns null for no matching account`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        accountRepository.saveForUser(persistedUser, account)

        // when
        val actual = accountRepository.findByAccountId(UUID.randomUUID())

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `findAllForUser returns empty list for no matching account`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        accountRepository.saveForUser(persistedUser, account)

        // when
        val actual = accountRepository.findAllForUser(UUID.randomUUID())

        // then
        assertThat(actual).isEmpty()
    }

    @Test
    fun `findAllForUser returns list of matching accounts`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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

        val otherAccount = Account(
            name = "My  other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        // when
        val actual = accountRepository.findAllForUser(persistedUser.userId)

        // then
        assertThat(actual).containsExactly(persistedAccount, persistedOtherAccount)
    }

    @Test
    fun `updateAccount is possible`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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
        val actual = accountRepository.updateAccount(
            persistedAccount.copy(
                balance = 999.0
            )
        )

        // then
        assertThat(actual).isNotNull
        assertThat(accountRepository.findByAccountId(actual.accountId)!!.balance).isEqualTo(999.0)
    }

    @Test
    fun `updateAccount fails if account does not exist`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
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

        // when + then
        assertThatThrownBy {
            accountRepository.updateAccount(account)
        }.isInstanceOf(IllegalStateException::class.java)

    }
}
