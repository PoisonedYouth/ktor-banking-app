package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.application.ApiResult.Failure
import com.poisonedyouth.application.ApiResult.Success
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.User
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
import java.time.format.DateTimeFormatter
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
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.deleteUser(persistedUser.userId.toString())

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(persistedUser.userId)
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
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.updateUser(
            UserDto(
                userId = persistedUser.userId,
                firstName = "Max",
                lastName = "DeMarco",
                birthdate = user.birthdate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                password = user.password
            )
        )


        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isEqualTo(persistedUser.userId)
        assertThat(userRepository.findByUserId(persistedUser.userId)!!.firstName).isEqualTo("Max")
        assertThat(userRepository.findByUserId(persistedUser.userId)!!.lastName).isEqualTo("DeMarco")
    }

    @Test
    fun `update user fails if user does not exist in database`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        // when
        val actual = userService.updateUser(
            UserDto(
                userId = user.userId,
                firstName = "Max",
                lastName = "DeMarco",
                birthdate = user.birthdate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                password = user.password
            )
        )


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `update user fails if password does not fulfill requirement`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.updateUser(
            UserDto(
                userId = persistedUser.userId,
                firstName = "Max",
                lastName = "DeMarco",
                birthdate = user.birthdate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                password = "NOT VALID"
            )
        )


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
        assertThat(userRepository.findByUserId(persistedUser.userId)!!.firstName).isEqualTo("John")
        assertThat(userRepository.findByUserId(persistedUser.userId)!!.lastName).isEqualTo("Doe")
    }

    @Test
    fun `findUserBy is possible`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.findUserByUserId(persistedUser.userId.toString())

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        (actual as Success).value.run {
            assertThat(this.userId).isEqualByComparingTo(persistedUser.userId)
            assertThat(this.firstName).isEqualTo("John")
            assertThat(this.lastName).isEqualTo("Doe")
            assertThat(this.birthdate).isEqualTo("01.01.1999")
            assertThat(this.password).isEqualTo("Ta1&tudol3lal54e")
            assertThat(this.created).isNotBlank
            assertThat(this.lastUpdated).isNotBlank
            assertThat(this.account).isEmpty()
        }
    }

    @Test
    fun `findUserBy fails if userId is invalid`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        userRepository.save(user)

        // when
        val actual = userService.findUserByUserId("invalid userId")

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `findUserBy fails if user not available in database`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        userRepository.save(user)

        // when
        val actual = userService.findUserByUserId(UUID.randomUUID().toString())

        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updatePassword is possible for existing user`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val userPasswordChangeDto = UserPasswordChangeDto(
            userId = persistedUser.userId,
            existingPassword = persistedUser.password,
            newPassword = "Ta1&zuxcv3lal54e"
        )

        // when
        val actual = userService.updatePassword(userPasswordChangeDto)


        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotNull
    }

    @Test
    fun `updatePassword fails for none existing user`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        userRepository.save(user)

        val userPasswordChangeDto = UserPasswordChangeDto(
            userId = UUID.randomUUID(),
            existingPassword = user.password,
            newPassword = "Ta1&zuxcv3lal54e"
        )

        // when
        val actual = userService.updatePassword(userPasswordChangeDto)


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updatePassword fails if existing password and new password are equal`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val userPasswordChangeDto = UserPasswordChangeDto(
            userId = persistedUser.userId,
            existingPassword = persistedUser.password,
            newPassword = "Ta1&tudol3lal54e"
        )

        // when
        val actual = userService.updatePassword(userPasswordChangeDto)


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.PASSWORD_ERROR)
    }

    @Test
    fun `updatePassword fails if new password does not fulfill the requirements`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        val userPasswordChangeDto = UserPasswordChangeDto(
            userId = persistedUser.userId,
            existingPassword = persistedUser.password,
            newPassword = "NOT VALID"
        )

        // when
        val actual = userService.updatePassword(userPasswordChangeDto)


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.PASSWORD_ERROR)
    }

    @Test
    fun `resetPassword is possible for existing user`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.resetPassword(persistedUser.userId.toString())

        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isNotEqualTo(persistedUser.password)
    }

    @Test
    fun `isValidUse is possible`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.isValidUser(userId = persistedUser.userId.toString(), password = persistedUser.password)


        // then
        assertThat(actual).isInstanceOf(Success::class.java)
        assertThat((actual as Success).value).isTrue
    }

    @Test
    fun `isValidUse fails if userId is invalid`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.isValidUser(userId = "INVALID_USERID", password = persistedUser.password)


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `isValidUse fails if user does not exist`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )

        // when
        val actual = userService.isValidUser(userId = user.userId.toString(), password = user.password)


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `isValidUse fails if password is incorrect`() {
        // given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(1999, 1, 1),
            password = "Ta1&tudol3lal54e"
        )
        val persistedUser = userRepository.save(user)

        // when
        val actual = userService.isValidUser(userId = persistedUser.userId.toString(), password = "WRONG_PASSWORD")


        // then
        assertThat(actual).isInstanceOf(Failure::class.java)
        assertThat((actual as Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }
}