package com.poisonedyouth.dependencyinjection

import com.poisonedyouth.application.AccountService
import com.poisonedyouth.application.AccountServiceImpl
import com.poisonedyouth.application.UserService
import com.poisonedyouth.application.UserServiceImpl
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.AccountRepositoryImpl
import com.poisonedyouth.persistence.TransactionRepository
import com.poisonedyouth.persistence.TransactionRepositoryImpl
import com.poisonedyouth.persistence.UserRepository
import com.poisonedyouth.persistence.UserRepositoryImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

val bankingAppModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<AccountRepository> { AccountRepositoryImpl() }
    single<TransactionRepository> { TransactionRepositoryImpl() }
    single<UserService> { UserServiceImpl(get()) }
    single<AccountService> { AccountServiceImpl(get(), get()) }
}

fun Application.setupKoin() {
    install(Koin) {
        SLF4JLogger()
        modules(bankingAppModule)
    }
}
