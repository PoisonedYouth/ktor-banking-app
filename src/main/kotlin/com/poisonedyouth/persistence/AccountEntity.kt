package com.poisonedyouth.persistence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

class AccountEntity(id: EntityID<Long>) : LongEntity(id) {
    var name by AccountTable.name
    var accountId by AccountTable.accountId
    var balance by AccountTable.balance
    var dispo by AccountTable.dispo
    var limit by AccountTable.limit
    var created by AccountTable.created
    var lastUpdated by AccountTable.lastUpdated
    var userEntity by UserEntity referencedOn AccountTable.user

    companion object : LongEntityClass<AccountEntity>(AccountTable)

}

object AccountTable : LongIdTable("account", "id") {
    val name = varchar("name", DEFAULT_VARCHAR_COLUMN_LENGTH)
    val accountId = uuid("account_id")
    val balance = double("balance")
    val dispo = double("dispo")
    val limit = double("limit")
    val created = datetime("created")
    val lastUpdated = datetime("last_updated")
    val user = reference("user", UserTable)
}
