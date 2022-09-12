package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.TransactionRepository
import com.poisonedyouth.persistence.UserRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
            transactionService.createTransaction(
                userId = persistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotNull
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
            transactionService.createTransaction(userId = UUID.randomUUID().toString(), transactionDto = transactionDto)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
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
            transactionService.createTransaction(
                userId = persistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `createTransaction fails if userId is invalid`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        userRepository.save(user)

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
            transactionService.createTransaction(userId = "INVALID_USERID", transactionDto = transactionDto)

        // then
        Assertions.assertThat(actual).isInstanceOf(Failure::class.java)
        Assertions.assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
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
            transactionService.createTransaction(
                userId = otherPersistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
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
            transactionService.createTransaction(
                userId = persistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_REQUEST_INVALID)
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
            transactionService.createTransaction(
                userId = persistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_REQUEST_INVALID)
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
            transactionService.createTransaction(
                userId = persistedUser.userId.toString(),
                transactionDto = transactionDto
            )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `deleteTransaction is possible`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        val persistedTransaction =
            transactionRepository.save(transaction)

        // when
        val actual = transactionService.deleteTransaction(persistedTransaction.transactionId.toString())

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotNull
        assertThat(accountRepository.findByAccountId(persistedTransaction.origin.accountId)!!.balance).isEqualTo(200.0)
        assertThat(accountRepository.findByAccountId(persistedTransaction.target.accountId)!!.balance).isEqualTo(-100.0)
        assertThat(transactionRepository.findByTransactionId(persistedTransaction.transactionId)).isNull()
    }

    @Test
    fun `deleteTransaction fails if transaction does not exist`() {
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

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        // when
        val actual = transactionService.deleteTransaction(transaction.transactionId.toString())

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND)
    }

    @Test
    fun `getTransaction is possible`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        val persistedTransaction =
            transactionRepository.save(transaction)

        // when
        val actual = transactionService.getTransaction(
            userId = persistedUser.userId.toString(),
            transactionId = persistedTransaction.transactionId.toString()
        )

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        (actual as Success).value.run {
            assertThat(this.transactionId).isEqualTo(persistedTransaction.transactionId)
            assertThat(this.origin).isEqualTo(persistedTransaction.origin.accountId)
            assertThat(this.target).isEqualTo(persistedTransaction.target.accountId)
            assertThat(this.amount).isEqualTo(persistedTransaction.amount)
        }
    }

    @Test
    fun `getTransaction fails if userId is invalid`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        val persistedTransaction =
            transactionRepository.save(transaction)

        // when
        val actual = transactionService.getTransaction(
            userId = "INVALID_USERID",
            transactionId = persistedTransaction.transactionId.toString()
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `getTransaction fails if transactionId is invalid`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        transactionRepository.save(transaction)

        // when
        val actual = transactionService.getTransaction(
            userId = persistedUser.userId.toString(),
            transactionId = "INVALID_TRANSACTIONID"
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `getTransaction fails if user does not exist`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        val persistedTransaction =
            transactionRepository.save(transaction)

        // when
        val actual = transactionService.getTransaction(
            userId = UUID.randomUUID().toString(),
            transactionId = persistedTransaction.transactionId.toString()
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `getTransaction fails if transaction does not exist`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        transactionRepository.save(transaction)

        // when
        val actual = transactionService.getTransaction(
            userId = persistedUser.userId.toString(),
            transactionId = UUID.randomUUID().toString()
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND)
    }

    @Test
    fun `getTransaction fails if transaction does not belong to user`() {
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
            limit = 100.0,
            balance = 100.0
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
            limit = 100.0,
            balance = 0.0
        )
        accountRepository.saveForUser(user = otherPersistedUser, account = otherAccount)

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )

        val persistedTransaction =
            transactionRepository.save(transaction)

        val extraUser = User(
            firstName = "Peter",
            lastName = "Griffin",
            birthdate = LocalDate.of(1985, 1, 7),
            password = "Ta1&tudol3lal54e"
        )
        val persistedExtraUser = userRepository.save(extraUser)

        // when
        val actual = transactionService.getTransaction(
            userId = persistedExtraUser.userId.toString(),
            transactionId = persistedTransaction.transactionId.toString()
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }
}