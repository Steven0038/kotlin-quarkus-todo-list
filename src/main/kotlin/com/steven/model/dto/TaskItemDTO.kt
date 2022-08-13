package com.steven.model.dto

import com.steven.util.NoArg
import org.bson.types.ObjectId
import javax.validation.constraints.NotNull

@NoArg
data class TaskItemDTO(

    /**
     * 名稱
     */
    @field:[NotNull]
    var itemName: String? = null,

    /**
     * 敘述
     */
    var itemDesc: String? = null,

    /**
     * 狀態
     */
    @field:[NotNull]
    var itemState: String? = null,

    /**
     * 代辦圖片 URL
     */
    var imageUrls: List<String>? = null,

    /**
     * 管理者 UserId
     */
    var adminUserId: ObjectId? = null,

    )
