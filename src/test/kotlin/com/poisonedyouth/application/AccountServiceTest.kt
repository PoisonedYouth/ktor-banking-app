package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.UserRepository
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

internal class AccountServiceTest : KoinTest {
    private lateinit var databaseFactory: TestDatabaseFactory
    private val accountService by inject<AccountService>()
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
    fun `createAccount is possible`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = AccountDto(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )

        // when
        val actual = accountService.createAccount(userId = persistedUser.userId.toString(), accountDto = account)

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotNull
    }

    @Test
    fun `createAccount fails if the user is not available in database`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        val account = AccountDto(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )

        // when
        val actual = accountService.createAccount(userId = user.userId.toString(), accountDto = account)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `createAccount fails if account limit is negative`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = AccountDto(
            name = "My Account",
            dispo = -100.0,
            limit = -100.0
        )

        // when
        val actual = accountService.createAccount(userId = persistedUser.userId.toString(), accountDto = account)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `createAccount fails if userId is invalid`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val account = AccountDto(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )

        // when
        val actual = accountService.createAccount(userId = "invalid userId", accountDto = account)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `createAccount fails if account is already available by accountId`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.createAccount(
            userId = persistedUser.userId.toString(), accountDto = AccountDto(
                accountId = persistedAccount.accountId,
                name = "My Other Account",
                dispo = persistedAccount.dispo,
                limit = persistedAccount.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.ACCOUNT_ALREADY_EXIST)
    }

    @Test
    fun `createAccount fails if account is already available by name`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.createAccount(
            userId = persistedUser.userId.toString(), accountDto = AccountDto(
                name = persistedAccount.name,
                dispo = persistedAccount.dispo,
                limit = persistedAccount.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.DATABASE_ERROR)
    }

    @Test
    fun `updateAccount is possible`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.updateAccount(
            userId = persistedUser.userId, accountDto = AccountDto(
                accountId = persistedAccount.accountId,
                name = "Other Account",
                dispo = persistedAccount.dispo,
                limit = persistedAccount.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(persistedAccount.accountId)
        assertThat(accountRepository.findByAccountId(persistedAccount.accountId)!!.name).isEqualTo("Other Account")
    }

    @Test
    fun `updateAccount fails if user does not exist in database`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.updateAccount(
            userId = UUID.randomUUID(), accountDto = AccountDto(
                accountId = persistedAccount.accountId,
                name = "Other Account",
                dispo = persistedAccount.dispo,
                limit = persistedAccount.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updateAccount fails if account not exist in database`() {
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
        // when
        val actual = accountService.updateAccount(
            userId = user.userId, accountDto = AccountDto(
                accountId = account.accountId,
                name = "Other Account",
                dispo = account.dispo,
                limit = account.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `updateAccount fails if account not belongs to given user`() {
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
            firstName = "Marco",
            lastName = "DeMarco",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val otherPersistedUser = userRepository.save(otherUser)

        // when
        val actual = accountService.updateAccount(
            userId = otherPersistedUser.userId, accountDto = AccountDto(
                accountId = account.accountId,
                name = "Other Account",
                dispo = account.dispo,
                limit = account.limit
            )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }

    @Test
    fun `deleteAccount is possible`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.deleteAccount(userId = persistedUser.userId, accountId = persistedAccount.accountId)

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(persistedAccount.accountId)
        assertThat(accountRepository.findAllForUser(persistedUser.userId)).isEmpty()
    }

    @Test
    fun `deleteAccount fails if user does not exist in database`() {
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
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.deleteAccount(userId = UUID.randomUUID(), accountId = persistedAccount.accountId)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `deleteAccount fails if account does not exist in database`() {
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

        // when
        val actual = accountService.deleteAccount(userId = persistedUser.userId, accountId = account.accountId)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun `deleteAccount fails if account does not belong to user`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val otherUser = User(
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedOtherUser = userRepository.save(otherUser)

        val account = Account(
            name = "My Account",
            dispo = -100.0,
            limit = 100.0
        )
        val persistedAccount = accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual =
            accountService.deleteAccount(userId = persistedOtherUser.userId, accountId = persistedAccount.accountId)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }
}