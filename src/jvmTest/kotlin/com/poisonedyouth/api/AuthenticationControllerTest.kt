package com.poisonedyouth.api

import com.poisonedyouth.KtorServerExtension
import com.poisonedyouth.createHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import java.util.*

@ExtendWith(KtorServerExtension::class)
internal class AuthenticationControllerTest : KoinTest {

    @Test
    fun checkAdministratorLogin() = runBlocking<Unit> {
        // given
        val administratorId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee"
        val password = "Ta1&tudol3lal54e"
        val client = createHttpClient()

        // when
        val response = client.get("http://localhost:8080/api/administrator/login") {
            accept(ContentType.Application.Json)
            parameter("administratorId",  Base64.getEncoder().encodeToString(administratorId.toByteArray()))
            parameter("password",  Base64.getEncoder().encodeToString(password.toByteArray()))
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<Boolean>>()
        assertThat(result.value).isTrue()
    }

    @Test
    fun `checkAdministratorLogin fails if parameter is missing`() = runBlocking<Unit> {
        // given
        val password = "Ta1&tudol3lal54e"
        val client = createHttpClient()

        // when
        val response = client.get("http://localhost:8080/api/administrator/login") {
            accept(ContentType.Application.Json)
            parameter("password", password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorCode).isEqualTo(ErrorCode.ADMINISTRATOR_NOT_FOUND)
    }

    @Test
    fun `checkAdministratorLogin fails if credentials are wrong`() = runBlocking<Unit> {
        // given
        val administratorId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee"
        val password = "WRONG_PASSWORD"
        val client = createHttpClient()

        // when
        val response = client.get("http://localhost:8080/api/administrator/login") {
            accept(ContentType.Application.Json)
            parameter("administratorId", Base64.getEncoder().encodeToString(administratorId.toByteArray()))
            parameter("password",  Base64.getEncoder().encodeToString(password.toByteArray()))
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        val result = response.body<ErrorDto>()
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }
}