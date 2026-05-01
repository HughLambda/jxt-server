package com.example.data

import org.jetbrains.exposed.v1.core.Table

object DeviceTable : Table("device") {
    val u_seq = varchar("u_seq", 36)
    val name = varchar("name", 100).nullable().uniqueIndex("uk_name")
    val status = integer("status").default(1)

    override val primaryKey = PrimaryKey(u_seq)
}