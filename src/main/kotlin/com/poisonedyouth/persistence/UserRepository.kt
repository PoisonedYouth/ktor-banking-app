package com.poisonedyouth.persistence

import com.poisonedyouth.domain.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

interface UserRepository {
    fun save(user: User): User

    fun delete(user: User)

    fun findByUserId(userId: UUID): User?
}

class UserRepositoryImpl : UserRepository {

    override fun save(user: User): User = transaction {
        val currentDateTime = LocalDateTime.now()
        val existingUser = UserEntity.find { UserTable.userId eq user.userId }.firstOrNull()
        if (existingUser == null) {
           UserEntity.new {
                userId = user.userId
                firstName = user.firstName
                lastName = user.lastName
                birthdate = user.birthdate
                password = user.password
                created = currentDateTime
                lastUpdated = currentDateTime
            }

            user.copy(
                created = currentDateTime,
                lastUpdated = currentDateTime
            )
        } else {
            existingUser.userId = user.userId
            existingUser.firstName = user.firstName
            existingUser.lastName = user.lastName
            existingUser.birthdate = user.birthdate
            existingUser.password = user.password
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
}

fun UserEntity.toUser() = User(
    userId = this.userId,
    firstName = this.firstName,
    lastName = this.lastName,
    birthdate = this.birthdate,
    password = this.password, created = this.created,
    lastUpdated = this.lastUpdated,
    accounts = this.accounts.map { it.toAccount() }
)
