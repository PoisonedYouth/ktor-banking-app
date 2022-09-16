package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.UUID

class AdministratorRepositoryTest : KoinTest {
    private lateinit var databaseFactory: TestDatabaseFactory
    private val administratorRepository by inject<AdministratorRepository>()

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
        checkDatabaseDefaults()
    }

    @AfterEach
    fun tearDownDatasource() {
        databaseFactory.close()
    }

    @Test
    fun `findByAdministratorId returns matching administrator`() {
        // given
        val administratorId = UUID.fromString("bdf79db3-1dfb-4ce2-b539-51de0cc703ee")
        val name = "DEFAULT ADMINISTRATOR"
        val password = "Ta1&tudol3lal54e"

        // when
        val actual = administratorRepository.findByAdministratorId(administratorId)

        // then
        actual!!.run {
            assertThat(this.administratorId).isEqualTo(administratorId)
            assertThat(this.name).isEqualTo(name)
            assertThat(this.password).isEqualTo(password)
        }
    }

    @Test
    fun `findByAdministratorId returns null for not matching administrator`() {
        // given
        val administratorId = UUID.fromString("bdf79db3-1dfb-4ce2-b539-111111111111")

        // when
        val actual = administratorRepository.findByAdministratorId(administratorId)

        // then
        assertThat(actual).isNull()
    }

}