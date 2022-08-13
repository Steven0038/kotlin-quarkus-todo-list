package com.steven.resource

import com.steven.constant.ApiError
import com.steven.exception.ApiNotFoundException
import com.steven.model.dto.PageRequest
import com.steven.model.dto.TaskCreateDTO
import com.steven.model.po.task.Task
import com.steven.model.vo.RowDataSetVO
import com.steven.repository.TaskRepository
import com.steven.service.TaskService
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import javax.inject.Inject
import javax.security.auth.login.LoginException
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


/**
 * FIXME: note, direct usage of entity object id in rest path is dangerous
 * TODO: authentication
 * 代辦管理 APIs
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
     * 代辦事項管理-新增代辦事項
     *
     * admin create a new [Task]
     *
     * @param dto [TaskCreateDTO]
     * @return created [Task]
     */
    @POST
    @Path("create")
    suspend fun create(
        @Valid dto: TaskCreateDTO
    ): Task {
        logger.info("[create] title: ${dto.title}, dto: $dto")

        val userId = checkLogin() ?: throw LoginException("admin not login!!")
        val adminUserId = dto.userId ?: userId

        return taskService.creatTaskByDto(dto, adminUserId)
    }

    /**
     * 管理員後台-取得代辦事項詳情
     * @param taskId [Task.id]
     * @return [Task]
     *
     */
    @GET
    @Path("{taskId}")
    suspend fun getTask(
        @RestPath taskId: ObjectId
    ): Task? = taskRepo.findById(taskId).awaitSuspending()

    /**
     * 管理員後台-代辦事項列表
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
     * 更新代辦事項
     *
     * @param taskId [Task.id]
     * @param reqTask [Task]
     * @return updated [Task]
     */
    @PUT
    @Path("{taskId}/update-state")
    suspend fun updateTaskState(
        @RestPath taskId: ObjectId,
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
