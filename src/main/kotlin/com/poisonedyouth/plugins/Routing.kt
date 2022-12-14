package com.poisonedyouth.plugins

import com.poisonedyouth.api.AccountController
import com.poisonedyouth.api.AuthenticationController
import com.poisonedyouth.api.TransactionController
import com.poisonedyouth.api.UserController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userController by inject<UserController>()
    val accountController by inject<AccountController>()
    val transactionController by inject<TransactionController>()
    val authenticationController by inject<AuthenticationController>()

    routing {
        route("/api/user") {
            authenticate("userAuthentication") {
                get("") {
                    userController.getExistingUser(call)
                }
                put("") {
                    userController.updateExistingUser(call)
                }
                delete("") {
                    userController.deleteUser(call)
                }
                put("/password") {
                    userController.updatePassword(call)
                }
            }
            post("") {
                userController.createNewUser(call)
            }
        }
        route("/api/account") {
            authenticate("userAuthentication") {
                get("/{accountId}") {
                    accountController.getExistingAccount(call)
                }
                post("") {
                    accountController.createNewAccount(call)
                }
                put("") {
                    accountController.updateExistingAccount(call)
                }
                delete("/{accountId}") {
                    accountController.deleteAccount(call)
                }
                get("/{accountId}/transaction") {
                    transactionController.getAllTransactionsForAccount(call)
                }
            }
        }
        route("/api/transaction") {
            authenticate("userAuthentication") {
                get("/{transactionId}") {
                    transactionController.getExistingTransaction(call)
                }
                post("") {
                    transactionController.createNewTransaction(call)
                }
            }
        }
        route("/api/administrator") {
            get("/login") {
                authenticationController.checkAdministratorLogin(call)
            }
            authenticate("administratorAuthentication") {
                delete("/transaction/{transactionId}") {
                    transactionController.deleteTransaction(call)
                }
                put("/user/{userid}/password") {
                    userController.resetUserPassword(call)
                }
                get("/user") {
                    userController.getAllUser(call)
                }
                get("/transaction") {
                    transactionController.getAllExistingTransaction(call)
                }
            }
        }
    }
}