package com.example.data

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.user").getString()
    val pwd = environment.config.property("database.password").getString()

    val dataSource = HikariDataSource().apply {
        jdbcUrl = url
        username = user
        password = pwd
        driverClassName = "org.mariadb.jdbc.Driver"
        maximumPoolSize = 10
        isReadOnly = false
    }

    Database.connect(dataSource)
}