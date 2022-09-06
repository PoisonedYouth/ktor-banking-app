package com.poisonedyouth.api

import com.poisonedyouth.application.UserDto
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.*

internal class UserControllerTest : KoinTest {

    private val userRepository by inject<UserRepository>()

    @Test
    fun `createNewUser`() = testApplication {
        environment {
            config = ApplicationConfig("application-test.conf")
        }
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val response = client.post("/api/user") {
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
        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        val body = response.body<UUID>()
        assertThat(body).isNotNull
        assertThat(userRepository.findByUserId(body)).isNotNull
    }
}