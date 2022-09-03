package com.poisonedyouth.dependencyinjection

import com.poisonedyouth.persistence.UserRepository
import com.poisonedyouth.persistence.UserRepositoryImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

val bankingAppModule = module {
    single<UserRepository> { UserRepositoryImpl() }
}

fun Application.setupKoin() {
    install(Koin) {
        SLF4JLogger()
        modules(bankingAppModule)
    }
}
