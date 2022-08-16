package com.steven.service

import arrow.core.Either
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import com.steven.model.dto.TaskCreateDTO
import com.steven.model.po.task.Task
import com.steven.model.po.task.TaskState
import com.steven.repository.TaskRepository
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 代辦事項 service
 */
@Singleton
class TaskService {

    @Inject
    private lateinit var taskRepo: TaskRepository

    /**
     * admin create initial [Task]
     *
     * @param dto [TaskCreateDTO]
     * @param adminUserId
     *
     * @return [Task]
     */
    suspend fun creatTaskByDto(
        dto: TaskCreateDTO,
        adminUserId: ObjectId
    ): Either<GlobalException, Task> = taskRepo.create(Task(dto, adminUserId))

    suspend fun findByObjId(id: ObjectId): Either<GlobalException, Task> = taskRepo.findByObjId(id)

    suspend fun updateTaskState(task: Task, newState: TaskState, userId: ObjectId): Task? {
        return taskRepo.findOneByIdAndUpdateState(task, newState, userId)
    }

    /**
     * list task with query filter
     *
     * @param pageReq [PageRequest]
     * @param keyword search keyword
     * @param since [Long] EpochMilli
     * @param until [Long] EpochMilli
     * @return [Task]
     */
    suspend fun listTaskPageWithFilter(pageReq: PageRequest, keyword: String?, since: Long?, until: Long?): List<Task> {
        val searchKey = keyword?.takeIf { it.isNotBlank() } ?: ""
        val startDate = since?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(0)
        val endDate = until?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(32472115200000)

        return taskRepo.find(
            "${Task::title.name} like ?1 or ${Task::description.name} like ?2 "
                    + "and ${Task::createdTime.name} >= ?3 and ${Task::createdTime.name} <= ?4",
            searchKey, searchKey, startDate, endDate
        )
            .page(pageReq.page - 1, pageReq.show).list()
            .awaitSuspending()
    }


}
