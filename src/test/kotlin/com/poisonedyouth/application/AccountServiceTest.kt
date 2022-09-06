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
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDate

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
        val actual = accountService.createAccount(userId = persistedUser.userId, accountDto = account)

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
        val actual = accountService.createAccount(userId = user.userId, accountDto = account)

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
        val actual = accountService.createAccount(userId = persistedUser.userId, accountDto = account)

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
        val persistedAccount =  accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.createAccount(userId = persistedUser.userId, accountDto = AccountDto(
            accountId = persistedAccount.accountId,
            name = "My Other Account",
            dispo = persistedAccount.dispo,
            limit = persistedAccount.limit
        )
        )

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.DATABASE_ERROR)
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
        val persistedAccount =  accountRepository.saveForUser(user = persistedUser, account = account)

        // when
        val actual = accountService.createAccount(userId = persistedUser.userId, accountDto = AccountDto(
            name = persistedAccount.name,
            dispo = persistedAccount.dispo,
            limit = persistedAccount.limit
        ))

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.DATABASE_ERROR)
    }
}