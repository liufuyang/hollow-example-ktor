package com.example

import com.example.gcs.InfoEntity
import com.example.gcs.Status
import gcs.customer.GcsBolbRetriever
import gcs.customer.GcsIndex
import gcs.customer.GcsWatcher
import gcs.producer.GcsAnnouncer
import gcs.producer.GcsIndexer
import gcs.producer.GcsPublisher
import gcs.repo.RepositoryProducer
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


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }

    val db = mapOf(
        1 to InfoEntity(1, "News 1", Status.NEW),
        2 to InfoEntity(2, "News 2", Status.NEW),
        3 to InfoEntity(3, "News 3", Status.NEW)
    ).toMutableMap()

    /** Hollow setup **/

    val publisher = GcsPublisher(this.environment.config, GcsIndexer(this.environment.config))
    val announcer = GcsAnnouncer(this.environment.config)

    val gcsIndex = GcsIndex(this.environment.config)
    val blobRetriever = GcsBolbRetriever(this.environment.config, gcsIndex)
    val announcementWatcher = GcsWatcher(this.environment.config)

    val producer = RepositoryProducer(
        announcer, this.environment.config,
        publisher, blobRetriever, announcementWatcher
    )
    producer.addDb(db)
    producer.initAndRestore()

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
            newInfo.timeUpdated = System.currentTimeMillis()
            println(newInfo)

            db[newInfo.id] = newInfo

            producer.runCycle()

            call.respond(HttpStatusCode.Created)
        }

        post("/snapshot") {

            producer.forceSnapshot()

            call.respond(HttpStatusCode.Created)
        }
    }

    val bucket: String = this.environment.config
        .propertyOrNull("carbon.gcs.bucket")?.getString()
        ?: "80"
    println(bucket)
}

