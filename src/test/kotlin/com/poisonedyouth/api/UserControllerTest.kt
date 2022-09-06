package com.poisonedyouth.api

import com.poisonedyouth.application.ErrorCode
import com.poisonedyouth.application.UserDto
import com.poisonedyouth.application.UserOverviewDto
import com.poisonedyouth.application.UserPasswordChangeDto
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserEntity
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDate
import java.util.*

internal class UserControllerTest : KoinTest {

    private val userRepository by inject<UserRepository>()

    companion object {
        lateinit var server: NettyApplicationEngine

        @BeforeAll
        @JvmStatic
        fun setup() {
            val env = applicationEngineEnvironment {
                config = ApplicationConfig("application-test.conf")
                // Public API
                connector {
                    host = "0.0.0.0"
                    port = 8080
                }
            }
            server = embeddedServer(Netty, env).start(false)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            server.stop(100, 100)
        }
    }

    @BeforeEach
    fun clearDatabase() {
        transaction { UserEntity.all().forEach { it.delete() } }
    }


    @Test
    fun `createNewUser`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        // when
        val response = client.post("http://localhost:8080/api/user") {
            setBody(
                UserDto(
                    firstName = "John",
                    lastName = "Doe",
                    birthdate = "01.01.2000",
                    password = "Ta1&tudol3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)).isNotNull
    }

    @Test
    fun `createNewUser fails if input is invalid`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        // when
        val response = client.post("http://localhost:8080/api/user") {
            setBody(
                UserDto(
                    firstName = "John",
                    lastName = "Doe",
                    birthdate = "01.01.2022",
                    password = "Ta1&tudol3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo(
            "Given UserDto 'UserDto(userId=null, firstName=John, lastName=Doe, " +
                    "birthdate=01.01.2022, password=Ta1&tudol3lal54e)' is not valid."
        )
        assertThat(result.errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `getExistingUser`() = runBlocking<Unit> {
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
        val response = client.get("http://localhost:8080/api/user/${user.userId}") {
            accept(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UserOverviewDto>>()
        result.run {
            assertThat(this.value.userId).isEqualTo(user.userId)
            assertThat(this.value.firstName).isEqualTo(user.firstName)
            assertThat(this.value.lastName).isEqualTo(user.lastName)
            assertThat(this.value.password).isEqualTo(user.password)
            assertThat(this.value.account).isEmpty()
        }
    }

    @Test
    fun `getExistingUser fails if userId is invalid`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        // when
        val response = client.get("http://localhost:8080/api/user/invalid_userId") {
            accept(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("Given userId 'invalid_userId' is not valid.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `updateExistingUser is possible`() = runBlocking<Unit> {
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
        val response = client.put("http://localhost:8080/api/user") {
            setBody(
                UserDto(
                    userId = user.userId,
                    firstName = "John",
                    lastName = "Doe",
                    birthdate = "01.01.2000",
                    password = "Ta1&tudol3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)).isNotNull
    }

    @Test
    fun `updateExistingUser fails if user does not exist`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        // when
        val userId = UUID.randomUUID()
        val response = client.put("http://localhost:8080/api/user") {
            setBody(
                UserDto(
                    userId = userId,
                    firstName = "John",
                    lastName = "Doe",
                    birthdate = "01.01.2000",
                    password = "Ta1&tudol3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("User with userId '$userId' does not exist in database.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `deleteUser is possible`() = runBlocking<Unit> {
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
        val response = client.delete("http://localhost:8080/api/user/${user.userId}") {
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)).isNull()
    }

    @Test
    fun `deleteUser fails if user does not exist`() = runBlocking<Unit> {
        // given
        val client = createHttpClient()

        userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        // when
        val userId = UUID.randomUUID()
        val response = client.delete("http://localhost:8080/api/user/${userId}") {
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("User with userId '$userId' does not exist in database.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    private fun createHttpClient(): HttpClient {
        val client = HttpClient() {
            install(ContentNegotiation) {
                jackson()
            }
        }
        return client
    }

    @Test
    fun `updatePassword is possible`() = runBlocking<Unit> {
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
        val response = client.put("http://localhost:8080/api/user/${user.userId}/password") {
            setBody(
                UserPasswordChangeDto(
                    userId = user.userId,
                    existingPassword = user.password,
                    newPassword = "Ta1&zuxcv3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)!!.password).isEqualTo("Ta1&zuxcv3lal54e")
    }

    @Test
    fun `updatePassword fails if new password is same as existing`() = runBlocking<Unit> {
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
        val response = client.put("http://localhost:8080/api/user/${user.userId}/password") {
            setBody(
                UserPasswordChangeDto(
                    userId = user.userId,
                    existingPassword = user.password,
                    newPassword = user.password
                )
            )
            contentType(ContentType.Application.Json)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("The new password cannot be the same as the existing one.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.PASSWORD_ERROR)
        assertThat(userRepository.findByUserId(user.userId)!!.password).isEqualTo(user.password)
    }
}