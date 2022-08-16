package com.steven

import com.steven.util.NoArg
import org.jboss.resteasy.reactive.PartType
import javax.ws.rs.FormParam
import javax.ws.rs.core.MediaType

/**
 * FIXME: data class unable to map form submit
 */
@NoArg
@Deprecated("unused")
data class TodoFormKT(
    @FormParam("title")
    @PartType(MediaType.TEXT_PLAIN)
    var title: String? = null,

    @FormParam("completed")
    @PartType(MediaType.TEXT_PLAIN)
    var completed: String? = null,

    @FormParam("priority")
    @PartType(MediaType.TEXT_PLAIN)
    var priority: Int? = null
) {
    @Deprecated("unused")
    fun convertIntoTodo(): Todo {
        val todo = Todo()
        todo.title = title
        todo.completed = "on" == completed
        todo.priority = priority
        return todo
    }

    @Deprecated("unused")
    fun updateTodo(toUpdate: Todo): Todo {
        toUpdate.title = title
        toUpdate.completed = "on" == completed
        toUpdate.priority = priority
        return toUpdate
    }
}