package com.steven.exception

import org.bson.types.ObjectId
import org.jboss.logging.Logger
import javax.ws.rs.core.Response

/**
 * ref. https://github.com/hmchangm/quarkus-reactive-kotlin
 */
sealed class GlobalException {
    data class DatabaseProblem(val e: Throwable) : GlobalException()
    data class FileReadError(val e: Throwable) : GlobalException()
    data class NotFoundName(val name: String) : GlobalException()
    data class NotFoundId(val id: ObjectId) : GlobalException()
    data class AddToDBError(val name: String) : GlobalException()
    class JsonSerializationFail(val e: Throwable) : GlobalException()

    companion object {
        private val LOG: Logger = Logger.getLogger(GlobalException::class.java)
        fun toResponse(taskError: GlobalException): Response = when (taskError) {
            is FileReadError -> Response.serverError().entity(taskError.e.stackTraceToString()).build()
            is JsonSerializationFail -> {
                LOG.error("Json Serialization Failed", taskError.e)
                Response.serverError().entity("Json Serialization Failed\n ${taskError.e.stackTraceToString()}")
                    .build()
            }
            is DatabaseProblem -> {
                LOG.error("db error", taskError.e)
                Response.serverError().entity("Db Connect Error \n ${taskError.e.stackTraceToString()}")
                    .build()
            }
            is NotFoundName -> Response.status(404).entity("Entity ${taskError.name} is not exist").build()
            is NotFoundId -> Response.status(404).entity("Entity ${taskError.id} is not exist").build()
            is AddToDBError -> Response.serverError().entity("Entity ${taskError.name} add to db failed").build()
        }
    }
}