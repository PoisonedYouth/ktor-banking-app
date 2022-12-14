package com.poisonedyouth.persistence

import com.poisonedyouth.TestDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class AdministratorEntityTest {
    private lateinit var databaseFactory: TestDatabaseFactory

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
    fun `creating new administrator is possible`() {
        // given + when
        val administrator = transaction {
            AdministratorEntity.new {
                administratorId = UUID.randomUUID()
                name = "Admin"
                password = "passw0rd".toByteArray()
                secretKey = "secretKey".toByteArray()
                iv = "iv".toByteArray()
                created = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
                lastUpdated = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
            }
        }

        // then
        assertThat(transaction { AdministratorEntity.findById(administrator.id) }).isNotNull
    }

    @Test
    fun `deleting an administrator is possible`() {
        // given
        val persistedAdministrator = transaction {
            AdministratorEntity.new {
                administratorId = UUID.randomUUID()
                name = "Admin"
                password = "passw0rd".toByteArray()
                secretKey = "secretKey".toByteArray()
                iv = "iv".toByteArray()
                created = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
                lastUpdated = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
            }
        }

        // when
        transaction { persistedAdministrator.delete() }

        // then
        assertThat(transaction { AdministratorEntity.findById(persistedAdministrator.id) }).isNull()
    }

    @Test
    fun `editing an administrator is possible`() {
        // given
        val persistedAdministrator = transaction {
            AdministratorEntity.new {
                administratorId = UUID.randomUUID()
                name = "Admin"
                password = "passw0rd".toByteArray()
                secretKey = "secretKey".toByteArray()
                iv = "iv".toByteArray()
                created = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
                lastUpdated = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
            }
        }

        // when
        transaction { persistedAdministrator.name = "Other Admin" }

        // then
        assertThat(
            transaction { AdministratorEntity.findById(persistedAdministrator.id)!!.name }
        ).isEqualTo("Other Admin")
    }

    @Test
    fun `find an administrator is possible`() {
        // given
        transaction {
            AdministratorEntity.new {
                administratorId = UUID.randomUUID()
                name = "Admin"
                password = "passw0rd".toByteArray()
                secretKey = "secretKey".toByteArray()
                iv = "iv".toByteArray()
                created = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
                lastUpdated = LocalDateTime.of(2000, 1, 1, 0, 0, 2)
            }
        }

        // when
        val actual = transaction { AdministratorEntity.find { AdministratorTable.name eq "Admin" }.firstOrNull() }

        // then
        assertThat(actual).isNotNull
    }
}
