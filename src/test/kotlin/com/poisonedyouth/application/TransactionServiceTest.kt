package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.UserRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDate
import java.util.*

internal class TransactionServiceTest : KoinTest {
    private lateinit var databaseFactory: TestDatabaseFactory
    private val transactionService by inject<TransactionService>()
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
    fun `createTransaction is possible`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0,
            balance = 200.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = persistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Success::class.java)
        Assertions.assertThat((actual as Success).value).isNotNull
    }

    @Test
    fun `createTransaction fails if user does not exist`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = UUID.randomUUID(), transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `createTransaction fails if account does not exist`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = persistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `createTransaction fails if account does not belong to user`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = otherPersistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }

    @Test
    fun `createTransaction fails if account has not enough balance`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = 99.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = persistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_REQUEST_INVALID)
    }

    @Test
    fun `createTransaction fails if amount is above limit`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = 200.0,
            limit = 50.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = 100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = persistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_REQUEST_INVALID)
    }

    @Test
    fun `createTransaction fails if amount is negative`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = Account(
            name = "My Account",
            dispo = 200.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = persistedUser, account = account)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        val otherAccount = Account(
            name = "Other Account",
            dispo = -100.0,
            limit = 100.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transactionDto = TransactionDto(
            origin = account.accountId,
            target = otherAccount.accountId,
            amount = -100.0
        )

        // when
        val actual =
            transactionService.createTransaction(userId = persistedUser.userId, transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }
}