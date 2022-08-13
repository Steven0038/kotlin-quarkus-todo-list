package com.steven.model.po.task

import org.bson.types.ObjectId

/**
 * 代辦事項
 */
data class TaskItem(

    /**
     * ref.[Task.id]
     */
    var taskId: ObjectId? = null,

    /**
     * 代辦事項名稱
     */
    var itemName: String? = null,

    /**
     * 代辦事項敘述
     */
    var itemDesc: String? = null,
)
