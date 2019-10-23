package com.example

import com.example.hollow.generated.InfoEntity
import com.example.hollow.generated.InfoEntityAPI
import com.netflix.hollow.api.consumer.HollowConsumer
import com.netflix.hollow.api.consumer.fs.HollowFilesystemAnnouncementWatcher
import com.netflix.hollow.api.consumer.fs.HollowFilesystemBlobRetriever
import com.netflix.hollow.api.consumer.index.HashIndex
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import java.io.File
import java.util.stream.Collectors


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(InfoEntity::class.java, InfoEntityAdapter())
        }
    }

    /** Hollow setup **/
    val localPublishDir = File("../hollow-ktor-producer/target/publish")

    val blobRetriever = HollowFilesystemBlobRetriever(localPublishDir.toPath())
    val announcementWatcher = HollowFilesystemAnnouncementWatcher(localPublishDir.toPath())

    val consumer = HollowConsumer.withBlobRetriever(blobRetriever)
        .withAnnouncementWatcher(announcementWatcher)
        .withGeneratedAPIClass(InfoEntityAPI::class.java)
        .build()

    consumer.triggerRefresh()

    // Setting up an id index
    val uniqueIndex = InfoEntity.uniqueIndex(consumer)
    // The line below is important. Without adding index onto the consumer refresh listener, the
    // index won't get updated
    consumer.addRefreshListener(uniqueIndex)

    // Setting up an Hash Index
    val builder = HashIndex.from(consumer, InfoEntity::class.java!!)
    val nameIndex: HashIndex<InfoEntity, String> = builder.usingPath("name.value", String::class.java)
    consumer.addRefreshListener(nameIndex)

    // Setting up an Hash Index on Enum
    val statusIndex: HashIndex<InfoEntity, String> = builder.usingPath("status._name", String::class.java)
    consumer.addRefreshListener(statusIndex)

    /** End of Hollow setup **/

    routing {

        get("/info") {
            val infoEntityAPI = consumer.getAPI() as InfoEntityAPI
            call.respond(infoEntityAPI.getAllInfoEntity().stream().collect(Collectors.toList()))
        }

        get("/info/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalStateException("Must provide id")
            val infoEntity = uniqueIndex.findMatch(id)
            if (infoEntity != null) {
                return@get call.respond(infoEntity)
            } else {
                return@get call.respond(HttpStatusCode.NoContent)
            }
        }

        get("/info/name/{name}") {
            val name = call.parameters["name"] ?: throw IllegalStateException("Must provide name")
            val infoEntities = nameIndex.findMatches(name).collect(Collectors.toList())
            if (infoEntities.isNotEmpty()) {
                return@get call.respond(infoEntities)
            } else {
                return@get call.respond(HttpStatusCode.NoContent)
            }
        }

        get("/info/status/{status}") {
            val status = call.parameters["status"] ?: throw IllegalStateException("Must provide status")
            val infoEntities = statusIndex.findMatches(status).collect(Collectors.toList())
            if (infoEntities.isNotEmpty()) {
                return@get call.respond(infoEntities)
            } else {
                return@get call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

