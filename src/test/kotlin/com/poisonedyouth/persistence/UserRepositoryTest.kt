package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.User
import io.ktor.util.reflect.instanceOf
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

internal class UserRepositoryTest : KoinTest {

    private lateinit var databaseFactory: TestDatabaseFactory
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
    fun `save persists new user to database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(
                2022, 1, 1, 1, 0, 0
            ),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )

        // when
        val actual = userRepository.save(user)

        // then
        assertThat(actual).isNotNull
        assertThat(transaction { UserEntity.find { UserTable.userId eq actual.userId }.single() }).isNotNull
    }

    @Test
    fun `save fails if user with firstname, lastname and birthdate already exists`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(
                2022, 1, 1, 1, 0, 0
            ),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        userRepository.save(user)

        // when + then
        assertThatThrownBy {
            userRepository.save(
                user.copy(
                    userId = UUID.randomUUID()
                )
            )
        }.isInstanceOf(ExposedSQLException::class.java)
    }


    @Test
    fun `save updates existing user to database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        val actual = userRepository.save(user)

        // when
        val updatedUser = userRepository.save(
            actual.copy(
                firstName = "Max"
            )
        )

        // then
        assertThat(updatedUser).isNotNull
        assertThat(transaction { UserEntity.all().count() }).isEqualTo(1)
        assertThat(transaction {
            UserEntity.find { UserTable.userId eq actual.userId }.single().firstName
        }).isEqualTo("Max")
    }

    @Test
    fun `delete removes existing user from database`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        val userNew = userRepository.save(user)

        // when
        userRepository.delete(userNew)

        // then
        assertThat(transaction { UserEntity.find { UserTable.userId eq user.userId }.singleOrNull() }).isNull()
    }

    @Test
    fun `delete throws exception if try to delete user without id`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )

        // when + then
        assertThatThrownBy { userRepository.delete(user) }.instanceOf(IllegalStateException::class)
    }

    @Test
    fun `delete throws exception if try to delete user with not existing id`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )

        // when + then
        assertThatThrownBy { userRepository.delete(user) }.instanceOf(IllegalStateException::class)
    }

    @Test
    fun `findByUserId returns matching user`() {
        // given
        val user = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        val userNew = userRepository.save(user)

        // when
        val actual = userRepository.findByUserId(userNew.userId)

        // then
        assertThat(actual).isNotNull
        actual?.run {
            assertThat(this.userId).isEqualTo(userNew.userId)
            assertThat(this.firstName).isEqualTo(userNew.firstName)
            assertThat(this.lastName).isEqualTo(userNew.lastName)
            assertThat(this.created.toEpochSecond(ZoneOffset.UTC)).isEqualTo(
                userNew.created.toEpochSecond(ZoneOffset.UTC)
            )
            assertThat(this.lastUpdated.toEpochSecond(ZoneOffset.UTC)).isEqualTo(
                userNew.lastUpdated.toEpochSecond(
                    ZoneOffset.UTC
                )
            )
            assertThat(this.accounts).isEmpty()
        }
    }

    @Test
    fun `findAll returns matching user`() {
        // given
        val user1 = User(
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        val userNew1 = userRepository.save(user1)
        val user2 = User(
            userId = UUID.randomUUID(),
            firstName = "Max",
            lastName = "DeMarco",
            birthdate = LocalDate.of(2000, 1, 1),
            password = "Ta1&tudol3lal54e",
            created = LocalDateTime.of(2022, 1, 1, 1, 0, 0),
            lastUpdated = LocalDateTime.of(2022, 1, 2, 1, 0, 0),
            accounts = listOf()
        )
        val userNew2 = userRepository.save(user2)

        // when
        val actual = userRepository.findAll()

        // then
        assertThat(actual.map { it.userId }).containsExactlyInAnyOrder(user1.userId, user2.userId)
    }
}
