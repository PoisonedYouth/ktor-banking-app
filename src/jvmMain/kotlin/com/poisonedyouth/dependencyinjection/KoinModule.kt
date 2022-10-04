package com.poisonedyouth.dependencyinjection

import com.poisonedyouth.api.AccountController
import com.poisonedyouth.api.AuthenticationController
import com.poisonedyouth.api.TransactionController
import com.poisonedyouth.api.UserController
import com.poisonedyouth.application.AccountService
import com.poisonedyouth.application.AccountServiceImpl
import com.poisonedyouth.application.AdministratorService
import com.poisonedyouth.application.AdministratorServiceImpl
import com.poisonedyouth.application.TransactionService
import com.poisonedyouth.application.TransactionServiceImpl
import com.poisonedyouth.application.UserService
import com.poisonedyouth.application.UserServiceImpl
import com.poisonedyouth.persistence.AccountRepository
import com.poisonedyouth.persistence.AccountRepositoryImpl
import com.poisonedyouth.persistence.AdministratorRepository
import com.poisonedyouth.persistence.AdministratorRepositoryImpl
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
    single<AdministratorRepository> { AdministratorRepositoryImpl() }
    single<UserService> { UserServiceImpl(get()) }
    single<AccountService> { AccountServiceImpl(get(), get()) }
    single<TransactionService> { TransactionServiceImpl(get(), get(), get()) }
    single<AdministratorService> { AdministratorServiceImpl(get()) }
    single { UserController(get()) }
    single { AccountController(get()) }
    single { TransactionController(get()) }
    single { AuthenticationController(get()) }
}

fun Application.setupKoin() {
    install(Koin) {
        SLF4JLogger()
        modules(bankingAppModule)
    }
}
