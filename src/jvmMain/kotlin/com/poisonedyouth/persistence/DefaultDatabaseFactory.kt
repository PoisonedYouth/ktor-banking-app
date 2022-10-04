package com.poisonedyouth.persistence

import com.poisonedyouth.configuration.ApplicationConfiguration
import com.poisonedyouth.security.EncryptionManager
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

fun setupDatabase(appConfig: ApplicationConfiguration): HikariDataSource {
    val defaultDatabaseFactory = DefaultDatabaseFactory(appConfig)
    defaultDatabaseFactory.connect()
    return defaultDatabaseFactory.dataSource
}

fun checkDatabaseDefaults() {
    transaction {
        if (AdministratorEntity.all().count() == 0L) {
            val encryptionResult = EncryptionManager.encrypt("Ta1&tudol3lal54e")
            AdministratorEntity.new {
                administratorId = UUID.fromString("bdf79db3-1dfb-4ce2-b539-51de0cc703ee")
                name = "DEFAULT ADMINISTRATOR"
                password = encryptionResult.ciphterText
                secretKey = encryptionResult.secretKey
                iv = encryptionResult.initializationVector
                created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                lastUpdated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
            }
        }
    }
}

class DefaultDatabaseFactory(appConfig: ApplicationConfiguration) : DatabaseFactory {

    private val dbConfig = appConfig.databaseConfig
    lateinit var dataSource: HikariDataSource

    override fun connect() {
        dataSource = hikari()
        Database.connect(dataSource)
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = dbConfig.driverClass
        config.jdbcUrl = dbConfig.url
        config.username = dbConfig.user
        config.password = dbConfig.password
        config.maximumPoolSize = dbConfig.maxPoolSize
        config.isAutoCommit = true
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }
}
