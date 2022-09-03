package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.domain.User
import org.assertj.core.api.Assertions.assertThat
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
            password = "passw0rd",
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
}
