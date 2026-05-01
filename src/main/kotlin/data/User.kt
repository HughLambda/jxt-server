package com.example.data

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json


object UserTable : Table("user") {
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 50)
    val role = integer("role")
    val tel = varchar("tel", 20).uniqueIndex("uk_tel")
    val pass = varchar("pass", 100).nullable()
    val dev = json<Array<String>>("dev", Json.Default).nullable()

    override val primaryKey = PrimaryKey(uuid)
}