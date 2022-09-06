package com.poisonedyouth.api

import com.poisonedyouth.application.UserDto
import com.poisonedyouth.application.UserOverviewDto
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
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
        val body = response.body<UUID>()
        assertThat(body).isNotNull
        assertThat(userRepository.findByUserId(body)).isNotNull
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
        val errorMessage = response.bodyAsText()
        assertThat(errorMessage).isEqualTo(
            "Given UserDto 'UserDto(userId=null, firstName=John, lastName=Doe, " +
                    "birthdate=01.01.2022, password=Ta1&tudol3lal54e)' is not valid."
        )
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
        val body = response.body<UserOverviewDto>()
        body.run {
            assertThat(this.userId).isEqualTo(user.userId)
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
        val body = response.bodyAsText()
        assertThat(body).isEqualTo("Given userId 'invalid_userId' is not valid.")
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
        val body = response.body<UUID>()
        assertThat(body).isNotNull
        assertThat(userRepository.findByUserId(body)).isNotNull
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
        val body = response.bodyAsText()
        assertThat(body).isEqualTo("User with userId '$userId' does not exist in database.")
    }

    private fun createHttpClient(): HttpClient {
        val client = HttpClient() {
            install(ContentNegotiation) {
                jackson()
            }
        }
        return client
    }
}