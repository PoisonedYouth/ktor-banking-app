package com.poisonedyouth.api

import com.poisonedyouth.KtorServerExtension
import com.poisonedyouth.application.BIRTH_DATE_FORMAT
import com.poisonedyouth.application.ErrorCode
import com.poisonedyouth.application.UserDto
import com.poisonedyouth.application.UserDtoAdministrator
import com.poisonedyouth.application.UserOverviewDto
import com.poisonedyouth.application.UserPasswordChangeDto
import com.poisonedyouth.createHttpClient
import com.poisonedyouth.domain.User
import com.poisonedyouth.persistence.UserEntity
import com.poisonedyouth.persistence.UserRepository
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import java.time.format.DateTimeFormatter
import java.util.*

@ExtendWith(KtorServerExtension::class)
internal class UserControllerTest : KoinTest {

    private val userRepository by inject<UserRepository>()

    @BeforeEach
    fun clearDatabase() {
        transaction { UserEntity.all().forEach { it.delete() } }
    }


    @Test
    fun createNewUser() = runBlocking<Unit> {
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
    fun getExistingUser() = runBlocking {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val client = createHttpClient(userId = user.userId.toString(), password = user.password)

        // when
        val response = client.get("http://localhost:8080/api/user") {
            accept(ContentType.Application.Json)
        }

        // then
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
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val client = createHttpClient(userId = "invalid_userId", password = user.password)

        // when
        val response = client.get("http://localhost:8080/api/user") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("Authentication for user with userId 'invalid_userId' failed.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updateExistingUser is possible`() = runBlocking<Unit> {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val client = createHttpClient(userId = user.userId.toString(), password = user.password)

        // when
        val response = client.put("http://localhost:8080/api/user") {
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
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)).isNotNull
    }

    @Test
    fun `updateExistingUser fails if user does not exist`() = runBlocking<Unit> {
        // given
        val userId = UUID.randomUUID()
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val client = createHttpClient(userId = userId.toString(), password = user.password)

        // when
        val response = client.put("http://localhost:8080/api/user") {
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
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage)
            .isEqualTo(
                "Authentication for user with userId '${
                    userId
                }' failed."
            )
    }

    @Test
    fun `deleteUser is possible`() = runBlocking {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val client = createHttpClient(userId = user.userId.toString(), password = user.password)

        // when
        val response = client.delete("http://localhost:8080/api/user") {
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)).isNull()
    }

    @Test
    fun `deleteUser fails if user does not exist`() = runBlocking<Unit> {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val userId = UUID.randomUUID()

        val client = createHttpClient(userId = userId.toString(), password = user.password)

        // when
        val response = client.delete("http://localhost:8080/api/user") {
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo(
            "Authentication for user with userId '${userId}' failed."
        )
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updatePassword is possible`() = runBlocking<Unit> {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val client = createHttpClient(userId = user.userId.toString(), password = user.password)

        // when
        val response = client.put("http://localhost:8080/api/user/password") {
            setBody(
                UserPasswordChangeDto(
                    userId = user.userId,
                    existingPassword = user.password,
                    newPassword = "Ta1&zuxcv3lal54e"
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<UUID>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(result.value)!!.password).isEqualTo("Ta1&zuxcv3lal54e")
    }

    @Test
    fun `updatePassword fails if new password is same as existing`() = runBlocking<Unit> {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val client = createHttpClient(userId = user.userId.toString(), password = user.password)

        // when
        val response = client.put("http://localhost:8080/api/user/password") {
            setBody(
                UserPasswordChangeDto(
                    userId = user.userId,
                    existingPassword = user.password,
                    newPassword = user.password
                )
            )
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("The new password cannot be the same as the existing one.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.PASSWORD_ERROR)
        assertThat(userRepository.findByUserId(user.userId)!!.password).isEqualTo(user.password)
    }

    @Test
    fun `resetPassword is possible`() = runBlocking<Unit> {
        // given
        val user = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val client = createHttpClient(userId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee", password = "Ta1&tudol3lal54e")

        // when
        val response = client.put("http://localhost:8080/api/administrator/user/${user.userId}/password") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<String>>()
        assertThat(result).isNotNull
        assertThat(userRepository.findByUserId(user.userId)!!.password).isEqualTo(result.value)
    }

    @Test
    fun `resetPassword fails if user does not exist`() = runBlocking<Unit> {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        val client = createHttpClient(userId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee", password = "Ta1&tudol3lal54e")

        // when
        val response = client.put("http://localhost:8080/api/administrator/user/${user.userId}/password") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        val result = response.body<ErrorDto>()
        assertThat(result.errorMessage).isEqualTo("User with userId '${user.userId}' does not exist in database.")
        assertThat(result.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun getAllUser() = runBlocking<Unit> {
        // given
        val user1 = userRepository.save(
            User(
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )
        val user2 = userRepository.save(
            User(
                firstName = "Max",
                lastName = "DeMarco",
                birthdate = LocalDate.of(1999, 1, 1),
                password = "Ta1&tudol3lal54e"
            )
        )

        val client = createHttpClient(userId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee", password = "Ta1&tudol3lal54e")

        // when
        val response = client.get("http://localhost:8080/api/administrator/user") {
            accept(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val result = response.body<SuccessDto<List<UserDtoAdministrator>>>()
        result.run {
            assertThat(this.value[0].userId).isEqualTo(user1.userId)
            assertThat(this.value[0].firstName).isEqualTo(user1.firstName)
            assertThat(this.value[0].lastName).isEqualTo(user1.lastName)
            assertThat(this.value[0].birthdate).isEqualTo(
                user1.birthdate.format(
                    DateTimeFormatter.ofPattern(
                        BIRTH_DATE_FORMAT
                    )
                )
            )

            assertThat(this.value[1].userId).isEqualTo(user2.userId)
            assertThat(this.value[1].firstName).isEqualTo(user2.firstName)
            assertThat(this.value[1].lastName).isEqualTo(user2.lastName)
            assertThat(this.value[1].birthdate).isEqualTo(
                user2.birthdate.format(
                    DateTimeFormatter.ofPattern(
                        BIRTH_DATE_FORMAT
                    )
                )
            )
        }
    }
}