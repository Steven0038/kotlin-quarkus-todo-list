package com.steven.model.dto

import com.steven.util.NoArg
import org.bson.types.ObjectId
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*
import javax.validation.constraints.NotNull

@NoArg
@Schema(name = "TaskCreateDTO", description = "Task representation")
data class TaskCreateDTO(

    var title: String? = null,

    var description: String? = null,

//    var items: List<TaskItemDTO>? = null,

    @Schema(required = true)
    @field:[NotNull]
    var startDate: Date? = null,

    @Schema(required = true)
    @field:[NotNull]
    var deprecateDate: Date? = null,

    var imageUrls: List<String>? = null,

    var userId: ObjectId? = null,
)
