package com.example

import com.example.hollow.generated.InfoEntity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


class InfoEntityAdapter : JsonSerializer<InfoEntity> {

    override fun serialize(
        src: InfoEntity, typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {

        val obj = JsonObject()
        obj.addProperty("id", src.id)
        obj.addProperty("name", src.name.value)
        obj.addProperty("status", src.status._name)
        obj.addProperty("timeUpdated", src.timeUpdated)

        return obj
    }
}