package com.example

import com.example.data.Device
import com.example.data.Services
import com.example.data.Status
import com.example.task.PullResp
import com.example.task.ReportReq
import com.example.task.TaskList
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.ucasoft.ktor.simpleCache.cacheOutput
import io.ktor.server.request.receive
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureRouting() {
    routing {
        //////
        ///////还有心跳没有实现
        //////
        //devices
        get("/tp") {
            val deviceUUID:String = call.request.queryParameters["uuid"]
                ?:return@get call.respond(PullResp(success = false))
            val device: Device? = Services.deviceService.readById(deviceUUID)

            //init device if not exist
            if (device == null) {
                Services.deviceService.create(Device(deviceUUID,deviceUUID, Status.ONLINE))
            }else {
                //online
                if (device.status == Status.OFFLINE || device.status == Status.PRINTING) {
                    Services.deviceService.update(Device(deviceUUID,device.name, Status.ONLINE))
                }
                //and something else
                //this way

                val tid = TaskList.brPopQue(deviceUUID)?:
                return@get call.respond(
                    PullResp(false,null)
                )
                val task = TaskList.hGetTask(tid) ?:
                return@get call.respond(
                    PullResp(false,null)
                )
                Services.deviceService.update(Device(deviceUUID,device.name, Status.PRINTING))
                call.respond(PullResp(true,task))
            }
        }


        get("/tr") {
            val req = call.receive<ReportReq>()
            if (!req.finished) {
                val task = TaskList.hGetTask(req.id)
                task?.let {
                    TaskList.lPushQue(it.device,it.id)
                }
            }
            //respond??????????????????
        }



        ///

        ///

        get("/") {
            call.respondText("Hello, World!")
        }
        cacheOutput(2.seconds) {
            get("/short") {
                call.respond(Random.nextInt().toString())
            }
        }
        cacheOutput {
            get("/default") {
                call.respond(Random.nextInt().toString())
            }
        }
        webSocket("/ws") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}