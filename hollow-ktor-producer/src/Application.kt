package com.example

import com.netflix.hollow.api.consumer.fs.HollowFilesystemAnnouncementWatcher
import com.netflix.hollow.api.consumer.fs.HollowFilesystemBlobRetriever
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

    val db = mapOf(
        1 to InfoEntity(1, "News 1", Status.NEW, 0),
        2 to InfoEntity(2, "News 2", Status.NEW, 1),
        3 to InfoEntity(3, "News 3", Status.NEW, 2)
    ).toMutableMap()

    /** Hollow setup **/
    val localPublishDir = File("target/publish")

    val publisher = HollowFilesystemPublisher(localPublishDir.toPath())
    val announcer = HollowFilesystemAnnouncer(localPublishDir.toPath())
    val blobRetriever = HollowFilesystemBlobRetriever(localPublishDir.toPath())
    val announcementWatcher = HollowFilesystemAnnouncementWatcher(localPublishDir.toPath())

    val producer = HollowProducer
        .withPublisher(publisher)
        .withAnnouncer(announcer)
        .buildIncremental()

    producer.initializeDataModel(InfoEntity::class.java)
    val latestAnnouncedVersion = announcementWatcher.getLatestVersion()
    producer.restore(latestAnnouncedVersion, blobRetriever)

    producer.runIncrementalCycle { state ->
        for (e in db.values)
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
            call.respond(db.values)
        }

        post("/info") {
            val newInfo = call.receive<InfoEntity>()
            println(newInfo)

            db[newInfo.id] = newInfo

            producer.runIncrementalCycle { state ->
                state.addOrModify(newInfo)
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}

