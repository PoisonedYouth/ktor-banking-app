package com.poisonedyouth.persistence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

const val DEFAULT_VARCHAR_COLUMN_LENGTH = 255

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    var userId by UserTable.userId
    var firstName by UserTable.firstName
    var lastName by UserTable.lastName
    var birthdate by UserTable.birthdate
    var password by UserTable.password
    var created by UserTable.created
    var lastUpdated by UserTable.lastUpdated
    val accounts by AccountEntity optionalReferrersOn  AccountTable.user

    companion object : LongEntityClass<UserEntity>(UserTable)
}

object UserTable : LongIdTable("user", "id") {
    val userId = uuid("user_id").uniqueIndex()
    val firstName = varchar("first_name", DEFAULT_VARCHAR_COLUMN_LENGTH)
    val lastName = varchar("last_name", DEFAULT_VARCHAR_COLUMN_LENGTH)
    val birthdate = date("birthdate")
    val password = varchar("password", DEFAULT_VARCHAR_COLUMN_LENGTH)
    val created = datetime("created")
    val lastUpdated = datetime("last_updated")
}
