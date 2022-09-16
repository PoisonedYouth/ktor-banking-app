package com.poisonedyouth.application

import com.poisonedyouth.TestDatabaseFactory
import com.poisonedyouth.dependencyinjection.bankingAppModule
import com.poisonedyouth.persistence.checkDatabaseDefaults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.UUID

class AdministratorServiceTest : KoinTest {
    private lateinit var databaseFactory: TestDatabaseFactory
    private val administratorService by inject<AdministratorService>()

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
    fun `isValidAdministrator is possible`() {
        // given
        val administratorId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee"
        val password = "Ta1&tudol3lal54e"

        // when
        val actual = administratorService.isValidAdministrator(administratorId = administratorId, password = password)

        // then
        assertThat(actual).isInstanceOf(ApiResult.Success::class.java)
        assertThat((actual as ApiResult.Success).value).isTrue
    }

    @Test
    fun `isValidAdministrator fails if administratorId is invalid`() {
        // given
        val administratorId = "INVALID_ADMINISTRATOR_ID"
        val password = "Ta1&tudol3lal54e"

        // when
        val actual = administratorService.isValidAdministrator(administratorId = administratorId, password = password)

        // then
        assertThat(actual).isInstanceOf(ApiResult.Failure::class.java)
        assertThat((actual as ApiResult.Failure).errorCode).isEqualTo(ErrorCode.MAPPING_ERROR)
    }

    @Test
    fun `isValidAdministrator fails if administratorId does not exist`() {
        // given
        val administratorId = UUID.randomUUID().toString()
        val password = "Ta1&tudol3lal54e"

        // when
        val actual = administratorService.isValidAdministrator(administratorId = administratorId, password = password)

        // then
        assertThat(actual).isInstanceOf(ApiResult.Failure::class.java)
        assertThat((actual as ApiResult.Failure).errorCode).isEqualTo(ErrorCode.ADMINISTRATOR_NOT_FOUND)
    }

    @Test
    fun `isValidAdministrator fails if password does not match`() {
        // given
        val administratorId = "bdf79db3-1dfb-4ce2-b539-51de0cc703ee"
        val password = "WRONG PASSWORD"

        // when
        val actual = administratorService.isValidAdministrator(administratorId = administratorId, password = password)

        // then
        assertThat(actual).isInstanceOf(ApiResult.Failure::class.java)
        assertThat((actual as ApiResult.Failure).errorCode).isEqualTo(ErrorCode.NOT_ALLOWED)
    }
}