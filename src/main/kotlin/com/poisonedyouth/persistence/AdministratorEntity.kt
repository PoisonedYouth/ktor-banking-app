package com.poisonedyouth.persistence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

class AdministratorEntity(id: EntityID<Long>) : LongEntity(id) {
    var administratorId by AdministratorTable.administratorId
    var name by AdministratorTable.name
    var password by AdministratorTable.password
    var secretKey by AdministratorTable.secretKey
    var iv by AdministratorTable.iv
    var created by AdministratorTable.created
    var lastUpdated by AdministratorTable.lastUpdated

    companion object : LongEntityClass<AdministratorEntity>(AdministratorTable)
}

object AdministratorTable : LongIdTable("administrator", "id") {
    val administratorId = uuid("administrator_id")
    val name = varchar("name", DEFAULT_VARCHAR_COLUMN_LENGTH)
    val password = binary("password")
    val secretKey = binary("secret_key")
    val iv = binary("iv")
    val created = datetime("created")
    val lastUpdated = datetime("last_updated")
}