package com.steven.service

import arrow.core.Either
import arrow.core.flatMap
import com.steven.model.po.Todo
import com.steven.model.dto.TodoForm
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import com.steven.model.po.task.Task
import com.steven.repository.TodoRepository
import org.bson.types.ObjectId
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 代辦事項 service
 */
@Singleton
class TodoService {

    @Inject
    private lateinit var todoRepo: TodoRepository

    suspend fun creatTaskByDto(dto: Todo): Either<GlobalException, Todo> = todoRepo.create(Todo(dto))

    suspend fun findByObjId(id: ObjectId): Either<GlobalException, Todo> = todoRepo.findByObjId(id)

    /**
     * list task with query filter
     *
     * @param pageReq [PageRequest]
     * @param keyword search keyword
     * @param since [Long] EpochMilli
     * @param until [Long] EpochMilli
     * @return [Task]
     */
    suspend fun listTaskPageWithFilter(pageReq: PageRequest, keyword: String?, since: Long?, until: Long?): Either<GlobalException.DatabaseProblem, List<Todo>> {
        val searchKey = keyword?.takeIf { it.isNotBlank() } ?: ""
        val startDate = since?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(0)
        val endDate = until?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(32472115200000)

        return todoRepo.findPagesByFilter(pageReq, searchKey, startDate, endDate)
    }

    suspend fun listTaskWithFilter(keyword: String?): Either<GlobalException.DatabaseProblem, List<Todo>> =
        todoRepo.findByFilter(keyword?.takeIf { it.isNotBlank() } ?: "")

    suspend fun findOneAndUpdate(id: ObjectId, todoForm: TodoForm) = this.findByObjId(id).flatMap { todo -> todoRepo.updateTodo(todoForm.updateTodo(todo)) }


}
