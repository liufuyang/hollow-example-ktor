package com.example.gcs

import com.netflix.hollow.core.write.objectmapper.HollowPrimaryKey

@HollowPrimaryKey(fields=["id"])
data class InfoEntity(
    val id: Int,
    val name: String,
    val status: Status

) {
    var timeUpdated: Long = System.currentTimeMillis()
}

enum class Status {
    NEW, OLD
}