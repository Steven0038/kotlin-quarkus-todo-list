package com.steven.resource.api

import com.steven.constant.ApiError
import com.steven.exception.ApiNotFoundException
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import com.steven.model.dto.TaskCreateDTO
import com.steven.model.po.task.Task
import com.steven.model.vo.RowDataSetVO
import com.steven.repository.TaskRepository
import com.steven.service.TaskService
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bson.types.ObjectId
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import javax.inject.Inject
import javax.security.auth.login.LoginException
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


/**
 * FIXME: note, direct usage of entity object id in rest path is dangerous
 * TODO: authentication
 * restful task APIs, just for the purpose of demonstration open API,
 * it use dependents collection in DB and not shown in the todolist UI
 */
@ExperimentalCoroutinesApi
@Path("api/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TaskResource {

    @Inject
    lateinit var logger: Logger

    @Inject
    private lateinit var taskRepo: TaskRepository

    @Inject
    private lateinit var taskService: TaskService

    /**
     * admin create a new [Task]
     * @param dto [TaskCreateDTO]
     * @return [Response] created [Task] or fail with [GlobalException]
     */
    @POST
    @Path("create")
    @Operation(
        operationId = "createTask",
        summary = "creat a new task",
        description = "create a task to add inside the list")
    @APIResponse(
        responseCode = "201",
        description = "task created",
        content = [Content(mediaType = MediaType.APPLICATION_JSON)])
    suspend fun create(
        @RequestBody(
            description = "task to create",
            required = true,
            content = [Content(schema = Schema(implementation = TaskCreateDTO::class))])
        @Valid dto: TaskCreateDTO
    ): Response? {
        logger.info("[create] title: ${dto.title}, dto: $dto")

        val userId = checkLogin() ?: throw LoginException("admin not login!!")
        val adminUserId = dto.userId ?: userId

        return taskService.creatTaskByDto(dto, adminUserId).fold(
            ifRight = { Response.ok(it).status(201).build() },
            ifLeft = { err -> GlobalException.toResponse(err) }
        )
    }

    /**
     * get the detail of a task by id
     * @param taskId [Task.id]
     * @return [Response] query [Task] or [GlobalException] if not found
     */
    @GET
    @Path("{taskId}")
    suspend fun getTask(
        @RestPath taskId: ObjectId
    ): Response =
        taskService.findByObjId(taskId).fold(
            ifRight = { task -> Response.ok(task).build() },
            ifLeft = { err -> GlobalException.toResponse(err) }
        )

    /**
     * TODO refactor to Arrow
     * list task list by keyword filter
     */
    @GET
    @Path("list")
    suspend fun listTasksPage(
        @Valid @BeanParam pageReq: PageRequest,
        @QueryParam("keyword") keyword: String?,
        @QueryParam("since") since: Long?,
        @QueryParam("until") until: Long?
    ): RowDataSetVO<Task> {

        val tasks = taskService.listTaskPageWithFilter(pageReq, keyword, since, until)

        return RowDataSetVO(
            list = tasks,
            page = pageReq.page,
            show = pageReq.show,
            total = tasks.size.toLong()
        )
    }

    /**
     * TODO refactor to Arrow
     * 更新代辦事項
     * @param taskId [Task.id]
     * @param reqTask [Task]
     * @return updated [Task]
     */
    @PUT
    @Path("{taskId}/update")
    @Operation(
        operationId = "updateTask",
        summary = "update a exist task",
        description = "update a task inside the list"
    )
    @APIResponse(
        responseCode = "200",
        description = "task updated",
        content = [Content(mediaType = MediaType.APPLICATION_JSON)]
    )
    suspend fun updateTaskState(
        @Parameter(
            description = "task id",
            required = true)
        @RestPath taskId: ObjectId,
        @RequestBody(
            description = "task to be updated",
            required = true,
            content = [Content(schema = Schema(implementation = Task::class))])
        @Valid reqTask: Task
    ): Task? {
        val adminId = checkLogin()
            ?: throw LoginException("admin not login!!")

        // TODO verify Task.state transformation

        val oriTask = taskRepo.findById(taskId).awaitSuspending()
            ?: throw ApiNotFoundException(ApiError.ENTITY_NOT_FOUND, "not found!!!")

        return reqTask.update<Task>().awaitSuspending()
            .also { logger.info("[updateTaskState] admin: $adminId do update from $oriTask to $reqTask success.") }
    }

    /**
     * TODO
     */
    suspend fun checkLogin(): ObjectId? =
        ObjectId("61c176f5d1ed5e52cfd0d15f")
}
