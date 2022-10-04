package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
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

class TransactionRepositoryTest : KoinTest {

    private lateinit var databaseFactory: TestDatabaseFactory
    private val accountRepository by inject<AccountRepository>()
    private val userRepository by inject<UserRepository>()
    private val transactionRepository by inject<TransactionRepository>()

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
    fun `save persists new transaction to database`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )

        // when
        val actual = transactionRepository.save(transaction)

        // then
        assertThat(actual).isNotNull
        assertThat(transaction {
            TransactionEntity.find { TransactionTable.transactionId eq actual.transactionId }.single()
        }).isNotNull
    }

    @Test
    fun `save throws exception if origin account not exists in database`() {
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
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = account,
            target = persistedOtherAccount,
            amount = 60.0
        )

        // when + then
        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `save throws exception if target account not exists in database`() {
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
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = account,
            amount = 60.0
        )

        // when + then
        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `save throws exception if transaction already exists in database`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = persistedAccount,
            amount = 60.0
        )
        transactionRepository.save(transaction)

        // when + then
        assertThatThrownBy { transactionRepository.save(transaction) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `findAllByAccount returns matching transaction for origin account`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        val actual = transactionRepository.findAllByAccount(persistedAccount)

        // then
        assertThat(actual.map { it.transactionId }).containsExactly(persistedTransaction.transactionId)
    }

    @Test
    fun `findAllByAccount returns matching transaction for target account`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        val actual = transactionRepository.findAllByAccount(persistedOtherAccount)

        // then
        assertThat(actual.map { it.transactionId }).containsExactly(persistedTransaction.transactionId)
    }

    @Test
    fun `findAllByAccount returns multipe matching transactions for account`() {
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

        val otherUser = User(
            userId = UUID.randomUUID(),
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedOtherUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        val otherTransaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedOtherAccount,
            target = persistedAccount,
            amount = 60.0
        )
        val persistedOtherTransaction = transactionRepository.save(otherTransaction)

        // when
        val actual = transactionRepository.findAllByAccount(persistedAccount)

        // then
        assertThat(actual.map { it.transactionId }).containsExactly(
            persistedTransaction.transactionId,
            persistedOtherTransaction.transactionId
        )
    }

    @Test
    fun `findByTransactionId returns matching transaction`() {
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

        val otherUser = User(
            userId = UUID.randomUUID(),
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedOtherUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        val actual = transactionRepository.findByTransactionId(persistedTransaction.transactionId)

        // then
        assertThat(actual).isEqualTo(persistedTransaction)
    }

    @Test
    fun `findByTransactionId returns null for no matching transaction`() {
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

        val otherUser = User(
            userId = UUID.randomUUID(),
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedOtherUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        transactionRepository.save(transaction)

        // when
        val actual = transactionRepository.findByTransactionId(UUID.randomUUID())

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `deleteTransaction deletes matching transaction`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        transactionRepository.delete(persistedTransaction)

        // then
        assertThat(transactionRepository.findByTransactionId(persistedTransaction.transactionId)).isNull()
    }

    @Test
    fun `deleteTransaction fails if transaction does not exist`() {
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
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedUser, otherAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )

        // when + then
        assertThatThrownBy {
            transactionRepository.delete(transaction)
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `findAll returns matching transactions`() {
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

        val otherUser = User(
            userId = UUID.randomUUID(),
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            accounts = listOf()
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedAccount = accountRepository.saveForUser(persistedUser, account)
        val otherAccount = Account(
            name = "Other account",
            accountId = UUID.randomUUID(),
            balance = 120.0,
            dispo = -1000.0,
            limit = 1000.0,
        )
        val persistedOtherAccount = accountRepository.saveForUser(persistedOtherUser, otherAccount)

        val transaction = Transaction(
            origin = persistedAccount,
            target = persistedOtherAccount,
            amount = 60.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        val otherTransaction = Transaction(
            origin = persistedOtherAccount,
            target = persistedAccount,
            amount = 25.0
        )
        val persistedOtherTransaction = transactionRepository.save(otherTransaction)

        // when
        val actual = transactionRepository.findAll()

        // then
        assertThat(actual.map { it.transactionId }).containsExactlyInAnyOrder(
            persistedTransaction.transactionId,
            persistedOtherTransaction.transactionId
        )
    }
}
