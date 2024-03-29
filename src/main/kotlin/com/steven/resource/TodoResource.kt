package com.steven.resource

import com.steven.model.po.Todo
import com.steven.model.dto.TodoForm
import com.steven.constant.ApiError
import com.steven.exception.ApiNotFoundException
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import com.steven.repository.TodoRepository
import com.steven.service.TodoService
import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.MultipartForm
import java.net.URI
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@ExperimentalCoroutinesApi
@Path("/todo")
class TodoResource {

    @CheckedTemplate
    object Templates {
        @JvmStatic external fun error(message: String): TemplateInstance // FIXME
        @JvmStatic external fun todo(todo: Todo, priorities: List<Int>, update: Boolean): TemplateInstance
        @JvmStatic external fun todos(todos: List<Todo>, totalCount: Long, priorities: List<Int>, filter: String, filtered: Boolean): TemplateInstance
    }

    val priorities: MutableList<Int> = IntStream.range(1, 6).boxed().collect(Collectors.toList())

    @Inject lateinit var logger: Logger
    @Inject private lateinit var todoRepo: TodoRepository
    @Inject private lateinit var todoService: TodoService

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/new")
    suspend fun addTodo(@MultipartForm todoForm: TodoForm): Any? { // data class unable accept form data
        val todo: Todo = todoForm.convertIntoTodo()
        return todoService.creatTaskByDto(todo).fold(
            ifRight = {
                Response.status(Response.Status.SEE_OTHER)
                    .location(URI.create("/todo"))
                    .build()
            },
            ifLeft = { Templates.error("add fail")  }
        )
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    suspend fun listTodos(
        @Valid @BeanParam pageReq: PageRequest,
        @QueryParam("filter") filter: String?,
        @QueryParam("since") since: Long?,
        @QueryParam("until") until: Long?
    ): TemplateInstance {
        return todoService.listTaskPageWithFilter(pageReq, filter, since, until).fold(
            ifRight = {
                Templates.todos(
                    it,
                    it.size.toLong(),
                    priorities,
                    filter ?: "",
                    filter != null && filter.isNotEmpty()
                )
            },
            ifLeft = { Templates.error("query fail, ${it.e}") }
        )
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun listTodosJson(@QueryParam("filter") filter: String?): Response {
        return todoService.listTaskWithFilter(filter).fold(
            ifRight = { todos ->  Response.ok(todos).build() },
            ifLeft = { err -> GlobalException.toResponse(err)}
        )
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/edit")
    suspend fun updateForm(@PathParam("id") id: String): TemplateInstance {
        return todoService.findByObjId(ObjectId(id)).fold(
            ifRight = { loaded -> Templates.todo(loaded, priorities, true) },
            ifLeft = { exception ->  Templates.error("Todo with id $id does not exist.") }
        )
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{id}/edit")
    suspend fun updateTodo(
        @PathParam("id") id: String,
        @MultipartForm todoForm: TodoForm
    ): Any {
        return todoService.findOneAndUpdate(ObjectId(id), todoForm).fold(
            ifRight = {
                Response.status(301)
                    .location(URI.create("/todo"))
                    .build()
            },
            ifLeft = { exception -> Templates.error("Todo update with id $id fail.") }
        )
    }

    @POST
    @Path("/{id}/delete")
    suspend fun deleteTodo(@PathParam("id") id: String): Response? {
        val oriTask = todoRepo.findById(ObjectId(id)).awaitSuspending()
            ?: throw ApiNotFoundException(ApiError.ENTITY_NOT_FOUND, "not found!!!")
        oriTask.delete().awaitSuspending().also { logger.info("id: $id already deleted!") }
        return Response.status(301)
            .location(URI.create("/todo"))
            .build()
    }
}
