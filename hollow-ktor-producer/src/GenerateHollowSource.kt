package com.example

import com.netflix.hollow.api.codegen.HollowAPIGenerator
import com.netflix.hollow.core.write.HollowWriteStateEngine
import com.netflix.hollow.core.write.objectmapper.HollowObjectMapper

fun main() {
    val writeEngine = HollowWriteStateEngine()
    val mapper = HollowObjectMapper(writeEngine)
    mapper.initializeTypeState(InfoEntity::class.java)

    val generator = HollowAPIGenerator.Builder().withAPIClassname("InfoEntityAPI")
        .withPackageName("com.example.hollow.generated")
        .withDataModel(writeEngine)
        .build()

    generator.generateFiles("../hollow-ktor-consumer/src/")
}