package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.persistence.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.*

internal class UserServiceTest : KoinTest {

    private lateinit var databaseFactory: TestDatabaseFactory
    private val userService by inject<UserService>()
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
    fun `create new user is possible`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.1999",
            password = "Ta1&tudol3lal54e"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotNull
    }

    @Test
    fun `create new user fails if userId already exists in database`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.1999",
            password = "Ta1&tudol3lal54e"
        )
        userService.createUser(user)


        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.DATABASE_ERROR)
    }


    @Test
    fun `create new user fails if birthdate is not parsable`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20-02-1999",
            password = "password"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if birthdate does not fulfill requirement`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2019",
            password = "password"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if password is too short`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2000",
            password = "password"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if password does not contain a lowercase character`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2000",
            password = "TTTTTTTTTTTTTTTT"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if password does not contain a uppercase character`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2000",
            password = "tttttttttttttttt"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if password does not contain a special character`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2000",
            password = "Atttttttttttttttt"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `create new user fails if password does not contain a digit`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.2000",
            password = "Attttttttttttttt1"
        )

        // when
        val actual = userService.createUser(user)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `delete existing user is possible`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.1999",
            password = "Ta1&tudol3lal54e"
        )
        val apiResult = userService.createUser(user)
        val userId = (apiResult as Success).value

        // when
        val actual = userService.deleteUser(userId.toString())

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(userId)
    }

    @Test
    fun `delete is not possible because user does not exist`() {
        // given
        val userId = UUID.randomUUID()

        // when
        val actual = userService.deleteUser(userId.toString())

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `delete is not possible because userId is not valid`() {
        // given
        val userId = "invalid_userId"

        // when
        val actual = userService.deleteUser(userId)

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `update existing user is possible`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.1999",
            password = "Ta1&tudol3lal54e"
        )
        val apiResult = userService.createUser(user)

        // when
        val actual = userService.updateUser(
            user.copy(
                userId = (apiResult as Success).value,
                firstName = "Max",
                lastName = "DeMarco"
            )
        )


        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(apiResult.value)
        assertThat(userRepository.findByUserId(apiResult.value)!!.firstName).isEqualTo("Max")
        assertThat(userRepository.findByUserId(apiResult.value)!!.lastName).isEqualTo("DeMarco")
    }

    @Test
    fun `update user fails if password does not fulfill requirement`() {
        // given
        val user = UserDto(
            firstName = "John",
            lastName = "Doe",
            birthdate = "20.02.1999",
            password = "Ta1&tudol3lal54e"
        )
        val apiResult = userService.createUser(user)

        // when
        val actual = userService.updateUser(
            user.copy(
                userId = (apiResult as Success).value,
                firstName = "Max",
                lastName = "DeMarco",
                password = "NOT VALID"
            )
        )


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
        assertThat(userRepository.findByUserId(apiResult.value)!!.firstName).isEqualTo("John")
        assertThat(userRepository.findByUserId(apiResult.value)!!.lastName).isEqualTo("Doe")
    }
}