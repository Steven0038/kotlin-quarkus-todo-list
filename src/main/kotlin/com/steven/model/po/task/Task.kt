package com.steven.model.po.task

import com.steven.model.dto.TaskCreateDTO
import com.steven.model.po.BaseMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.types.ObjectId
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.Instant
import java.util.*

/**
 * 代辦工作
 */
@MongoEntity
@Schema(name = "Task", description = "Task representation")
data class Task(


    /**
     * 標題
     */
    var title: String? = null,

    /**
     * 敘述
     */
    var description: String? = null,

//    /**
//     * 代辦事項清單
//     *
//     * ref.[TaskItem]
//     */
//    var items: List<TaskItem>? = null,

    /**
     * ref. [TaskState.value]
     */
    var state: String? = null,

    var userId: ObjectId? = null,

    /**
     * 生效日期
     */
    var startDate: Date? = null,

    /**
     * 失效日期
     */
    var deprecateDate: Date? = null,

    /**
     * 代辦圖片 URL
     */
    var imageUrls: List<String>? = null,


    ) : BaseMongoEntity<Task>() {

    /**
     * construct a create approach
     */
    constructor(dto: TaskCreateDTO, userId: ObjectId) : this() {
//        this.items = dto.items
        this.title = dto.title
        this.description = dto.description
        this.state = TaskState.ACTIVE().value
        this.userId = userId
        this.startDate = dto.startDate
        this.deprecateDate = dto.deprecateDate
        this.imageUrls = dto.imageUrls
        this.createdTime = Instant.now()
        this.lastModifiedTime = Instant.now()
    }
}

sealed class TaskState(var value: String, var msg: String) {
    class ACTIVE : TaskState("ACTIVE", "未完成")
    class FINISHED : TaskState("FINISHED", "已完成")
    class DEPRECATED : TaskState("DEPRECATED", "已過期失效")
}


