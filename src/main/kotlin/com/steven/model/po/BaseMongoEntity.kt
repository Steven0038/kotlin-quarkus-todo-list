package com.steven.model.po

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoEntity
import java.time.Instant

abstract class BaseMongoEntity<T>(

    var lastModifiedTime: Instant? = null,

    var createdTime: Instant = Instant.now()

) : ReactivePanacheMongoEntity()
