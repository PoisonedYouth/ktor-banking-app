package com.poisonedyouth.persistence

import com.poisonedyouth.domain.Administrator
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

interface AdministratorRepository {
    fun findByAdministratorId(administratorId: UUID): Administrator?
}

class AdministratorRepositoryImpl : AdministratorRepository {
    override fun findByAdministratorId(administratorId: UUID): Administrator? = transaction {
        AdministratorEntity.find { AdministratorTable.administratorId eq administratorId }.firstOrNull()
            ?.toAdministrator()
    }

}

fun AdministratorEntity.toAdministrator() = Administrator(
    administratorId = this.administratorId,
    name = this.name,
    password = this.password,
    created = this.created,
    lastUpdated = this.lastUpdated
)