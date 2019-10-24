package com.example

import com.netflix.hollow.api.producer.HollowProducer
import com.netflix.hollow.api.producer.fs.HollowFilesystemBlobStager
import com.netflix.hollow.core.write.HollowBlobWriter
import gcs.customer.GcsBolbRetriever
import gcs.customer.GcsIndex
import gcs.customer.GcsWatcher
import gcs.producer.GcsAnnouncer
import gcs.producer.GcsIndexer
import gcs.producer.GcsPublisher
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
import java.io.BufferedOutputStream
import java.nio.file.Files


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
//    val localPublishDir = File("target/publish")

    val publisher = GcsPublisher(this.environment.config, GcsIndexer(this.environment.config))
    val announcer = GcsAnnouncer(this.environment.config)

    val producer = HollowProducer
        .withPublisher(publisher)
        .withAnnouncer(announcer)
        .withNumStatesBetweenSnapshots(50)
        .buildIncremental()

//    val blobRetriever = HollowFilesystemBlobRetriever(localPublishDir.toPath())
//    val announcementWatcher = HollowFilesystemAnnouncementWatcher(localPublishDir.toPath())

    val gcsIndex = GcsIndex(this.environment.config)
    val blobRetriever = GcsBolbRetriever(this.environment.config, gcsIndex)
    val announcementWatcher = GcsWatcher(this.environment.config)

    producer.initializeDataModel(InfoEntity::class.java)
    producer.restore(announcementWatcher.getLatestVersion(), blobRetriever)

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

        get("/version") {
            call.respond(announcer.announcedVersion)
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

        post("/snapshot") {

            val temporaryProducer = HollowProducer
                .withPublisher(publisher)
                .withAnnouncer(announcer)
                .build()
            val writeEngine = temporaryProducer.getWriteEngine()
            writeEngine.prepareForNextCycle()

            val blobWriter = HollowBlobWriter(writeEngine)
            val entities = db.values
            val stager = HollowFilesystemBlobStager()
            val versionOfSnapshot = announcer.announcedVersion + 1L

            val blob = stager.openSnapshot(versionOfSnapshot)

            entities.forEach { temporaryProducer.getObjectMapper().add(it) }


            val output = BufferedOutputStream(Files.newOutputStream(blob.path))
            blobWriter.writeSnapshot(output)
            publisher.publish(blob)
            announcer.announce(versionOfSnapshot)

            producer.restore(versionOfSnapshot, blobRetriever)

            call.respond(HttpStatusCode.Created)
        }
    }

    val bucket: String = this.environment.config
        .propertyOrNull("carbon.gcs.bucket")?.getString()
        ?: "80"
    println(bucket)
}

