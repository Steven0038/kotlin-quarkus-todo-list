package com.steven.service

import arrow.core.Either
import com.steven.Todo
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import com.steven.model.po.task.Task
import com.steven.repository.TodoRepository
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
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

    val sort: Sort = Sort.ascending("completed")
        .and("priority", Sort.Direction.Descending)
        .and("title", Sort.Direction.Ascending)

    /**
     * list task with query filter
     *
     * @param pageReq [PageRequest]
     * @param keyword search keyword
     * @param since [Long] EpochMilli
     * @param until [Long] EpochMilli
     * @return [Task]
     */
    suspend fun listTaskPageWithFilter(pageReq: PageRequest, keyword: String?, since: Long?, until: Long?): List<Todo> {
        val page = pageReq.page
        val show = pageReq.show
        val searchKey = keyword?.takeIf { it.isNotBlank() } ?: ""
        val startDate = since?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(0)
        val endDate = until?.let { Instant.ofEpochMilli(it) } ?: Instant.ofEpochMilli(32472115200000)

        return todoRepo.find(
            "${Todo::title.name} like ?1"
                    + "and ${Todo::createdTime.name} >= ?2 and ${Todo::createdTime.name} <= ?3",
            sort,
            searchKey, startDate, endDate
        )
            .page(page - 1, show).list()
            .awaitSuspending()
    }

    suspend fun listTaskWithFilter(keyword: String?): List<Todo> {
        val searchKey = keyword?.takeIf { it.isNotBlank() } ?: ""
        return todoRepo.find(
            "${Todo::title.name} like ?1",
            sort,
            searchKey
        ).list().awaitSuspending()
    }

    suspend fun updateTodo(todo: Todo): Todo? {
        return todoRepo.findOneByIdAndUpdate(todo)
    }

}
