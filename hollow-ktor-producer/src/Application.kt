package com.example

import com.netflix.hollow.api.producer.HollowProducer
import com.netflix.hollow.api.producer.fs.HollowFilesystemAnnouncer
import com.netflix.hollow.api.producer.fs.HollowFilesystemPublisher
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.io.File


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }

    val entityList = listOf(
        InfoEntity(1, "News 1", Status.NEW, 0),
        InfoEntity(2, "News 2", Status.NEW, 1),
        InfoEntity(3, "News 3", Status.NEW, 2)
    ).toMutableList()

    /** Hollow setup **/
    val localPublishDir = File("target/publish")

    val publisher = HollowFilesystemPublisher(localPublishDir.toPath())
    val announcer = HollowFilesystemAnnouncer(localPublishDir.toPath())

    val producer = HollowProducer
        .withPublisher(publisher)
        .withAnnouncer(announcer)
        .buildIncremental()

    producer.runIncrementalCycle { state ->
        for (e in entityList)
            state.addIfAbsent(e)
    }
    /** End of Hollow setup **/

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/info") {
            call.respond(entityList)
        }

        post("/info") {
            val newInfo = call.receive<InfoEntity>()
            println(newInfo)

            val sameId = entityList.filter { it.id == newInfo.id }
            if (sameId.isEmpty()) {
                entityList.add(newInfo)
            } else {
                val ind = entityList.indexOfFirst { it.id == newInfo.id }
                entityList.removeAt(ind)
                entityList.add(newInfo)
            }

            producer.runIncrementalCycle { state ->
                state.addOrModify(newInfo)
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}

