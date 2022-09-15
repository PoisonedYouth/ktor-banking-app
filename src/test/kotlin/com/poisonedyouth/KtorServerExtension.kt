package com.poisonedyouth

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class KtorServerExtension : BeforeAllCallback, AfterAllCallback {
    companion object {
        private lateinit var server: NettyApplicationEngine
    }

    override fun beforeAll(context: ExtensionContext?) {
        val env = applicationEngineEnvironment {
            config = ApplicationConfig("application-test.conf")
            // Public API
            connector {
                host = "0.0.0.0"
                port = 8080
            }
        }
        server = embeddedServer(Netty, env).start(false)
    }

    override fun afterAll(context: ExtensionContext?) {
        server.stop(100, 100)
    }
}

fun createHttpClient(userId: String = "userId", password: String = "password"): HttpClient {
    val client = HttpClient {
        install(ContentNegotiation) {
            jackson()
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = userId, password = password)
                }
                realm = "Ktor Banking App"
            }
        }
    }
    return client
}