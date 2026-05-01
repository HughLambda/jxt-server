package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.ucasoft.ktor.simpleCache.cacheOutput
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureRouting() {
    routing {
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