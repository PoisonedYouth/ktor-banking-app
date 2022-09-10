package com.poisonedyouth.api

import com.poisonedyouth.KtorServerExtension
import com.poisonedyouth.application.AccountDto
import com.poisonedyouth.application.AccountOverviewDto
import com.poisonedyouth.application.ErrorCode
import com.poisonedyouth.application.TIME_STAMP_FORMAT
import com.poisonedyouth.application.UserDto
import com.poisonedyouth.application.UserOverviewDto
import com.poisonedyouth.createHttpClient
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.AccountEntity
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.UserEntity
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@ExtendWith(KtorServerExtension::class)
internal class AccountControllerTest : KoinTest {

    private val accountRepository by inject<AccountRepository>()
    private val userRepository by inject<UserRepository>()

    @BeforeEach
    fun clearDatabase() {
        transaction { AccountEntity.all().forEach { it.delete() } }
        transaction { UserEntity.all().forEach { it.delete() } }
    }

    @Test
    fun getExistingAccount() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val account = accountRepository.saveForUser(
            user = user, account = Account(
                name = "My account",
                balance = 100.0,
                dispo = -100.0,
                limit = 50.0
            )
        )

        // when
        val response = client.get("http://localhost:8080/api/user/${user.userId}/account/${account.accountId}") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<AccountOverviewDto>>()
        result.value.run {
            assertThat(this.accountId).isEqualTo(account.accountId)
            assertThat(this.balance).isEqualTo(account.balance)
            assertThat(this.dispo).isEqualTo(account.dispo)
            assertThat(this.limit).isEqualTo(account.limit)
            assertThat(this.name).isEqualTo(account.name)
            assertThat(this.created).isEqualTo(
                account.created.format(
                    DateTimeFormatter.ofPattern(
                        TIME_STAMP_FORMAT
                    )
                )
            )
            assertThat(this.lastUpdated).isEqualTo(
                account.lastUpdated.format(
                    DateTimeFormatter.ofPattern(TIME_STAMP_FORMAT)
                )
            )
        }
    }

    @Test
    fun `getExistingAccount fails if accountId is invalid`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        accountRepository.saveForUser(
            user = user, account = Account(
                name = "My account",
                balance = 100.0,
                dispo = -100.0,
                limit = 50.0
            )
        )

        // when
        val response = client.get("http://localhost:8080/api/user/${user.userId}/account/invalid_accountId") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage)
            .isEqualTo("Given userId '${user.userId}' or 'invalid_accountId' is not valid.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun createNewAccount() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        // when
        val response = client.post("http://localhost:8080/api/user/${user.userId}/account") {
            setBody(
                AccountDto(
                    name = "My Account",
                    dispo = -200.0,
                    limit = 100.0
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(accountRepository.findByAccountId(result.value)).isNotNull
    }

    @Test
    fun `createNewAccount fails if user does not exist`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        // when
        val response = client.post("http://localhost:8080/api/user/${user.userId}/account") {
            setBody(
                AccountDto(
                    name = "My Account",
                    dispo = -200.0,
                    limit = 100.0
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo(
            "User with userId '${user.userId}' does not exist in database."
        )
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun updateExistingAccount() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val account = accountRepository.saveForUser(user = user, account = Account(
            name = "My account",
            balance = 100.0,
            limit = 100.0,
            dispo = -200.0
        )
        )

        // when
        val response = client.put("http://localhost:8080/api/user/${user.userId}/account") {
            setBody(
                AccountDto(
                    accountId = account.accountId,
                    name = "My other Account",
                    dispo = -500.0,
                    limit = 200.0
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        accountRepository.findByAccountId(result.value)!!.run {
            assertThat(this.balance).isEqualTo(account.balance)
            assertThat(this.dispo).isEqualTo(-500.0)
            assertThat(this.limit).isEqualTo(200.0)
        }
    }

    @Test
    fun `updateExistingAccount fails if account does not exist yet`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val account = Account(
            name = "My account",
            balance = 100.0,
            limit = 100.0,
            dispo = -200.0
        )

        // when
        val response = client.put("http://localhost:8080/api/user/${user.userId}/account") {
            setBody(
                AccountDto(
                    accountId = account.accountId,
                    name = "My other Account",
                    dispo = -500.0,
                    limit = 200.0
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo(
            "Account with accountId '${account.accountId}' does not exist in database."
        )
        assertThat(result.errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }
}