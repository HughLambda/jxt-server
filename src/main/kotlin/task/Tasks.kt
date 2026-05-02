package com.example.task

import com.example.Utils
import kotlinx.serialization.Serializable
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig


@Serializable
data class Task(val id: String,val device: String,val msg:Msg)

@Serializable
data class Msg(val name: String,val text: String)

@Serializable
data class ReportReq(val id: String,val device: String,val finished: Boolean)

@Serializable
data class PullResp(val success: Boolean,val task: Task? = null)


object TaskList {
    ///
    //
    //
    //
    //warning: change to 6388
    private val pool = JedisPool(JedisPoolConfig(),"localhost",6379)
    fun conn(): Jedis = pool.resource

    fun lPushQue(uid:String,tid:String) {
        conn().use {
            it.lpush("task:queue:$uid",tid)
        }
    }
    fun brPopQue(uid:String) : String? {
        conn().use {
            val res = it.brpop(10,"task:queue:$uid")
            return res?.get(1)
        }
    }
    fun hmSetTask(task: Task) {
        conn().use {
            it.hset(
                "task:info:${task.id}",
                mapOf<String, String>(
                    "id" to task.id,
                    "device" to task.device,
                    "msg" to Utils.gson.toJson(task.msg)
                )
            )
        }
    }
    fun hGetTask(tid:String): Task? {
        conn().use {
            val map = it.hgetAll("task:info:$tid")
            if (map.isEmpty()) return null
            return Task(
                id = map["id"] ?: return null,
                device = map["device"] ?: return null,
                msg = Utils.gson.fromJson(map["msg"] , Msg::class.java)
            )
        }
    }
}