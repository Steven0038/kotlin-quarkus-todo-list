package com.steven.model.dto

import com.steven.util.NoArg
import org.bson.types.ObjectId
import java.util.*
import javax.validation.constraints.NotNull

@NoArg
data class TaskCreateDTO(

    var title: String? = null,

    var description: String? = null,

//    var items: List<TaskItemDTO>? = null,

    @field:[NotNull]
    var startDate: Date? = null,

    @field:[NotNull]
    var deprecateDate: Date? = null,

    var imageUrls: List<String>? = null,

    var userId: ObjectId? = null,
)
