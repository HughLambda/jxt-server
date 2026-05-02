package com.example

import com.example.data.Device
import com.example.data.DeviceService
import com.example.data.Role
import com.example.data.Status
import com.example.data.User
import com.example.data.UserService
import com.example.task.TaskList
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplication
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import kotlin.test.*

class ServerTest {
    val dataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:mariadb://192.168.0.104:3306/testdb?useSSL=false&serverTimezone=Asia/Shanghai"
        username = "hugo"
        password = "Hugo88888888"
        driverClassName = "org.mariadb.jdbc.Driver"
        maximumPoolSize = 10
        isReadOnly = false
    }

    val database = Database.connect(dataSource)
    val deviceService = DeviceService(database)
    val userService   = UserService(database)

    @Test
    fun `test root endpoint`() = testApplication {
        // loads default configuration
        configure()
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun testDeviceDBCreate() {

        runBlocking {
            deviceService.create("217")
            println()
            println(deviceService.readAll())
        }

    }
    @Test
    fun testDeviceDBDel() = runBlocking {
        deviceService.delete("217")
    }
    @Test
    fun testDeviceDBUpdate() {
        runBlocking {
            val device: Device? = deviceService.read("217")
            device?.let {
                deviceService.update(
                    Device(
                        it.u_seq,
                        device.name,
                        Status.ONLINE
                    )
                )
            }
        }
    }

    @Test
    fun testUserDBCreate()  = runBlocking {
        userService.create(User(
            uuid ="",
            name ="hyc",
            role = Role.PARENT,
            tel = "13300998877",
            pass = "",
            dev = arrayOf("217","216")
        ))
    }
    @Test
    fun testUserReadAll() = runBlocking {
        println(userService.readAll())
    }
    @Test
    fun testuserreadbytel() = runBlocking {
        println(userService.read("13300998877"))
    }


    @Test
    fun testTaskList() = testApplication {
    }

}
