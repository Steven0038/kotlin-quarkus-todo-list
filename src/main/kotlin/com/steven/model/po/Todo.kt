package com.steven.model.po

import com.steven.model.dto.TodoForm
import io.quarkus.mongodb.panache.common.MongoEntity
import java.time.Instant

@MongoEntity
data class Todo(
    var title: String? = null,
    var priority: Int? = null,
    var completed: Boolean? = null,
): BaseMongoEntity<Todo>() {

    constructor(dto: Todo) : this() {
        this.title = dto.title
        this.priority = dto.priority
        this.completed = dto.completed
        this.createdTime = Instant.now()
        this.lastModifiedTime = Instant.now()
    }

    /**
     * construct a create approach
     */
    constructor(dto: TodoForm) : this() {
        this.title = dto.title
        this.priority = dto.priority
        this.completed = ("on" == dto.completed)
        this.createdTime = Instant.now()
        this.lastModifiedTime = Instant.now()
    }

    /**
     * construct a update approach
     */
    constructor(ori: Todo, dto: TodoForm) : this() {
        this.id = ori.id
        this.title = dto.title
        this.priority = dto.priority
        this.completed = ("on" == dto.completed)
        this.lastModifiedTime = Instant.now()
    }
}