package com.poisonedyouth.persistence

import com.poisonedyouth.domain.Administrator
import com.poisonedyouth.security.EncryptionManager
import com.poisonedyouth.security.EncryptionResult
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

interface AdministratorRepository {
    fun findByAdministratorId(administratorId: UUID): Administrator?
}

class AdministratorRepositoryImpl : AdministratorRepository {
    override fun findByAdministratorId(administratorId: UUID): Administrator? = transaction {
        AdministratorEntity.find { AdministratorTable.administratorId eq administratorId }.firstOrNull()
            ?.toAdministrator()
    }

}

fun AdministratorEntity.toAdministrator(): Administrator {
    val encryptionResult = EncryptionResult(
        secretKey = this.secretKey,
        initializationVector = this.iv,
        ciphterText = this.password
    )
    return Administrator(
        administratorId = this.administratorId,
        name = this.name,
        password = EncryptionManager.decrypt(encryptionResult),
        created = this.created,
        lastUpdated = this.lastUpdated
    )
}