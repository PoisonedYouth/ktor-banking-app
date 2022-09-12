package com.poisonedyouth.api

import com.poisonedyouth.KtorServerExtension
import com.poisonedyouth.application.ErrorCode
import com.poisonedyouth.application.TransactionDto
import com.poisonedyouth.createHttpClient
import com.poisonedyouth.domain.Account
import com.poisonedyouth.domain.Transaction
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.AccountEntity
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.TransactionEntity
import com.poisonedyouth.persistence.TransactionRepository
import com.poisonedyouth.persistence.UserEntity
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDate
import java.util.UUID

@ExtendWith(KtorServerExtension::class)
internal class TransactionControllerTest : KoinTest {

    private val transactionRepository by inject<TransactionRepository>()
    private val accountRepository by inject<AccountRepository>()
    private val userRepository by inject<UserRepository>()

    @BeforeEach
    fun clearDatabase() {
        transaction { UserEntity.all().forEach { it.delete() } }
        transaction { TransactionEntity.all().forEach { it.delete() } }
        transaction { AccountEntity.all().forEach { it.delete() } }
    }

    @Test
    fun getExistingTransaction() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        val response = client.get(
            "http://localhost:8080//api/user/${user.userId}/transaction/" +
                "${persistedTransaction.transactionId}"
        ) {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<TransactionDto>>()
        result.value.run {
            assertThat(this.transactionId).isEqualTo(persistedTransaction.transactionId)
            assertThat(this.origin).isEqualTo(persistedTransaction.origin.accountId)
            assertThat(this.target).isEqualTo(persistedTransaction.target.accountId)
            assertThat(this.amount).isEqualTo(persistedTransaction.amount)
        }
    }

    @Test
    fun `getExistingTransaction fails if transactionId is invalid`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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

        val transaction = Transaction(
            origin = account,
            target = otherAccount,
            amount = 100.0
        )
        val persistedTransaction = transactionRepository.save(transaction)

        // when
        val response = client.get(
            "http://localhost:8080//api/user/${user.userId}/transaction/" +
                "INVALD_TRANSACTIONID"
        ) {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage)
            .isEqualTo("Given transactionId 'INVALD_TRANSACTIONID' or userId '${user.userId}' is not valid.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun createNewTransaction() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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
            limit = 300.0,
            balance = 400.0
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


        // when
        val response = client.post(
            "http://localhost:8080//api/user/${user.userId}/transaction"
        ) {
            setBody(TransactionDto(
                origin = account.accountId,
                target = otherAccount.accountId,
                amount = 234.0
            ))
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(transactionRepository.findByTransactionId(result.value)).isNotNull
    }

    @Test
    fun `createNewTransaction fails if origin account does not exist`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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
            limit = 300.0,
            balance = 400.0
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


        // when
        val response = client.post(
            "http://localhost:8080//api/user/${user.userId}/transaction"
        ) {
            setBody(TransactionDto(
                origin = account.accountId,
                target = otherAccount.accountId,
                amount = 234.0
            ))
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage)
            .isEqualTo("Account with accountId '${account.accountId}' does not exist in database.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND)
    }

    @Test
    fun deleteExistingTransaction() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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
            limit = 300.0,
            balance = 400.0
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
        val persistedTransaction = transactionRepository.save(transaction)


        // when
        val response = client.delete(
            "http://localhost:8080//api/administrator/transaction/${persistedTransaction.transactionId}"
        ) {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(transactionRepository.findByTransactionId(result.value)).isNull()
        assertThat(accountRepository.findByAccountId(account.accountId)!!.balance).isEqualTo(500.0)
        assertThat(accountRepository.findByAccountId(otherAccount.accountId)!!.balance).isEqualTo(-100.0)
    }

    @Test
    fun `deleteExistingTransaction if transaction does not exist`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

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
            limit = 300.0,
            balance = 400.0
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
        val response = client.delete(
            "http://localhost:8080//api/administrator/transaction/${transaction.transactionId}"
        ) {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage)
            .isEqualTo("Transaction with transactionId '${transaction.transactionId}' does not exist in database.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND)
    }
}