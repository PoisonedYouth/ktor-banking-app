package com.poisonedyouth.persistence

import com.poisonedyouth.domain.User
import com.poisonedyouth.security.EncryptionManager
import com.poisonedyouth.security.EncryptionResult
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

interface UserRepository {
    fun save(user: User): User

    fun delete(user: User)

    fun findByUserId(userId: UUID): User?

    fun findAll(): List<User>
}

class UserRepositoryImpl : UserRepository {

    override fun save(user: User): User = transaction {
        val currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val existingUser = UserEntity.find { UserTable.userId eq user.userId }.firstOrNull()
        if (existingUser == null) {
            val encryptionResult = EncryptionManager.encrypt(user.password)
            UserEntity.new {
                userId = user.userId
                firstName = user.firstName
                lastName = user.lastName
                birthdate = user.birthdate
                password = encryptionResult.ciphterText
                secretKey = encryptionResult.secretKey
                iv = encryptionResult.initializationVector
                created = currentDateTime
                lastUpdated = currentDateTime
            }

            user.copy(
                created = currentDateTime,
                lastUpdated = currentDateTime
            )
        } else {
            val encryptionResult = EncryptionManager.encrypt(user.password)
            existingUser.userId = user.userId
            existingUser.firstName = user.firstName
            existingUser.lastName = user.lastName
            existingUser.birthdate = user.birthdate
            existingUser.password = encryptionResult.ciphterText
            existingUser.secretKey = encryptionResult.secretKey
            existingUser.iv = encryptionResult.initializationVector
            existingUser.created = user.created
            existingUser.lastUpdated = currentDateTime
            user.copy(
                lastUpdated = currentDateTime
            )
        }
    }

    override fun delete(user: User): Unit = transaction {
        UserEntity.find { UserTable.userId eq user.userId }.firstOrNull().let {
            if (it == null) {
                error("User '${user.userId}' does not exist!")
            } else {
                it.delete()
            }
        }
    }

    override fun findByUserId(userId: UUID): User? = transaction {
        UserEntity.find { UserTable.userId eq userId }.firstOrNull()?.toUser()
    }

    override fun findAll(): List<User> = transaction {
        UserEntity.all().map { it.toUser() }
    }
}

fun UserEntity.toUser(): User {
    val encryptionResult = EncryptionResult(
        secretKey = this.secretKey,
        initializationVector = this.iv,
        ciphterText = this.password
    )
    return User(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        birthdate = this.birthdate,
        password = EncryptionManager.decrypt(encryptionResult),
        created = this.created.truncatedTo(ChronoUnit.SECONDS),
        lastUpdated = this.lastUpdated.truncatedTo(ChronoUnit.SECONDS),
        accounts = this.accounts.map { it.toAccount() }
    )
}
