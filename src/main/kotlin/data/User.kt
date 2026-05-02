package com.example.data

import com.example.data.UserTable.dev
import com.example.data.UserTable.name
import com.example.data.UserTable.pass
import com.example.data.UserTable.role
import com.example.data.UserTable.tel
import com.example.data.UserTable.uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.inTopLevelSuspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.json.json
import java.util.UUID


object UserTable : Table("user") {
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 50)
    val role = integer("role")
    val tel = varchar("tel", 20).uniqueIndex("uk_tel")
    val pass = varchar("pass", 100)
    val dev = json<Array<String>>("dev", Json.Default)

    override val primaryKey = PrimaryKey(uuid)
}

enum class Role(val code:Int) {
    ADMIN(0),
    TEACHER(1),
    PARENT(2);
    companion object {
        fun fromInt(code: Int): Role {
            return entries.find { it.code == code } ?:PARENT
        }
    }
}

@Serializable
data class User(
    val uuid:String,
    val name:String,
    val role:Role,
    val tel:String,
    val pass:String,
    val dev: Array<String>)

class UserService(private val database: Database) {
    init {
        //start
        transaction(database) {
            SchemaUtils.create(UserTable)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        inTopLevelSuspendTransaction { block() }
    }
    suspend fun create(user:User) {
        dbQuery {
            UserTable.insert {
                it[uuid] = UUID.randomUUID().toString()
                it[name] = user.name
                it[role] = user.role.code
                it[tel]  = user.tel
                it[pass] = user.pass
                it[dev]  = user.dev
            }
        }
    }
    suspend fun read(tel: String):User? {
        return dbQuery {
            UserTable.selectAll()
                .where { UserTable.tel eq tel }
                .map{User(
                    it[UserTable.uuid],
                    it[UserTable.name],
                    Role.fromInt(it[UserTable.role]),
                    it[UserTable.tel],
                    it[UserTable.pass],
                    it[UserTable.dev]
                )}
                .singleOrNull()
        }
    }
    suspend fun readAll():List<User> = dbQuery {
        UserTable.selectAll()
            .map{User(
                it[UserTable.uuid],
                it[UserTable.name],
                Role.fromInt(it[UserTable.role]),
                it[UserTable.tel],
                it[UserTable.pass],
                it[UserTable.dev]
            )}
    }
    suspend fun delete(tel: String)  = dbQuery {
        UserTable.deleteWhere {
            UserTable.tel.eq(tel)
        }
    }
    suspend fun update(user: User) = dbQuery {
        UserTable.update({ UserTable.uuid eq user.uuid}) {
            it[uuid] = UUID.randomUUID().toString()
            it[name] = user.name
            it[role] = user.role.code
            it[tel]  = user.tel
            it[pass] = user.pass
            it[dev]  = user.dev
        }
    }
}
