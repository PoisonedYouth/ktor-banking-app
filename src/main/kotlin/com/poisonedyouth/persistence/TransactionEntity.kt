package com.poisonedyouth.persistence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

class TransactionEntity(id: EntityID<Long>) : LongEntity(id) {
    var transactionId by TransactionTable.transactionId
    var originEntity by AccountEntity referencedOn TransactionTable.origin
    var targetEntity by AccountEntity referencedOn TransactionTable.target
    var amount by TransactionTable.amount
    var created by TransactionTable.created

    companion object : LongEntityClass<TransactionEntity>(TransactionTable)
}

object TransactionTable : LongIdTable("transaction", "id") {
    val transactionId = uuid("transaction_id")
    val origin = reference("origin", AccountTable.accountId)
    val target = reference("target", AccountTable.accountId)
    val amount = double("amount")
    val created = datetime("created")
}
