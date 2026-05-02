package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.inTopLevelSuspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

object DeviceTable : Table("device") {
    val u_seq = varchar("u_seq", 36)
    val name = varchar("name", 100).uniqueIndex("uk_name")
    val status = integer("status").default(1)

    override val primaryKey = PrimaryKey(u_seq)
}

@Serializable
data class Device(val u_seq : String, val name: String,val status: Status)


enum class Status (val code:Int){
    UNKNOWN(-2),
    PAPER_NEEDED(-1),
    PRINTING(1),
    ONLINE(0),
    OFFLINE(2);
    companion object {
        fun fromInt(code:Int): Status {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}


class DeviceService(private val database: Database) {
    init {
        //start table
        transaction(database) {
            SchemaUtils.create(DeviceTable)
        }
    }
    suspend fun create(name: String) = create(name, Status.OFFLINE)
    suspend fun create(name:String,status: Status) {
        create(Device(UUID.randomUUID().toString(),name,status))
    }
    suspend fun create(device: Device) = dbQuery {
        DeviceTable.insert {
            it[u_seq] = device.u_seq
            it[name] = device.name
            it[status] = device.status.code
        }
    }
    //wtf???
    suspend fun <T> dbQuery(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        inTopLevelSuspendTransaction { block() }
    }
    suspend fun read(name: String): Device? {
        return dbQuery {
            DeviceTable.selectAll()
                .where { DeviceTable.name eq name }
                .map { Device(it[DeviceTable.u_seq],
                    it[DeviceTable.name],
                    Status.fromInt(it[DeviceTable.status])) }
                .singleOrNull()
        }
    }
    suspend fun readAll(): List<Device> {
        return dbQuery {
            DeviceTable.selectAll()
                .map {
                    Device(it[DeviceTable.u_seq],
                        it[DeviceTable.name],
                        Status.fromInt(it[DeviceTable.status]))
                }
        }
    }
    suspend fun update(device: Device) {
        dbQuery {
            DeviceTable.update({ DeviceTable.u_seq eq device.u_seq}) {
                it[name] = device.name
                it[status] = device.status.code
            }
        }
    }
    suspend fun delete(name: String) {
        dbQuery {
            DeviceTable.deleteWhere { DeviceTable.name eq name }
        }
    }

    suspend fun readById(uuid:String): Device? {
        return dbQuery {
            DeviceTable.selectAll()
                .where { DeviceTable.u_seq eq uuid }
                .map {
                    Device(it[DeviceTable.u_seq],
                        it[DeviceTable.name],
                        Status.fromInt(it[DeviceTable.status]))
                }
                .singleOrNull()
        }
    }
}