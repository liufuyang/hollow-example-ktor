package com.example

import com.netflix.hollow.core.write.objectmapper.HollowPrimaryKey

@HollowPrimaryKey(fields=["id"])
data class InfoEntity(
    val id: Int,
    val name: String,
    val status: Status,
    val timeUpdated: Long
)

enum class Status {
    NEW, OLD
}