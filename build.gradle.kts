val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val junitVersion: String by project
val koinVersion: String by project

plugins {
    kotlin("multiplatform") version "1.7.10"
    application
}

group = "com.poisonedyouth"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
                implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")

                // exposed
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("com.zaxxer:HikariCP:5.0.1")

                // h2
                implementation("com.h2database:h2:2.1.214")

                // liquibase
                implementation("org.liquibase:liquibase-core:4.15.0")

                // koin
                implementation("io.insert-koin:koin-ktor:$koinVersion")
                implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
            }
        }
        val jvmTest by getting{
            dependencies{
                // koin
                implementation("io.insert-koin:koin-ktor:$koinVersion")
                implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

                // testing
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
                implementation("org.assertj:assertj-core:3.23.1")
                implementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
                implementation("io.insert-koin:koin-test:$koinVersion")
                implementation("io.insert-koin:koin-test-junit5:$koinVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.0.0-pre.332-kotlin-1.6.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.0.0-pre.332-kotlin-1.6.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.0-pre.332-kotlin-1.6.21")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}