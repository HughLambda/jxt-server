
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.websockets)
    //sessions
    implementation(ktorLibs.server.sessions)
    //data
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)

    implementation(libs.logback.classic)
    implementation(libs.ucasoft.ktorSimpleCache)
    implementation(libs.ucasoft.ktorSimpleRedisCache)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)

    //阻塞
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    implementation("com.zaxxer:HikariCP:5.0.1")
    //jedis

    implementation("redis.clients:jedis:4.4.3")
}
