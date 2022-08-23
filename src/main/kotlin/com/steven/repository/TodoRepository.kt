package com.steven.repository

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.toOption
import com.mongodb.client.model.Filters
import com.steven.model.po.Todo
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import java.time.Instant
import javax.inject.Singleton


@Singleton
class TodoRepository : BaseMongoRepository<Todo>() {

    val sort: Sort = Sort.ascending("completed")
        .and("priority", Sort.Direction.Descending)
        .and("title", Sort.Direction.Ascending)

    val create: suspend (task: Todo) -> Either<GlobalException, Todo> = { task ->
        Either.catch {
            task.save()
        }.mapLeft { GlobalException.DatabaseProblem(it) }
    }

    suspend fun findByObjId(id: ObjectId): Either<GlobalException, Todo> = Either.catch {
        this.findOne(Filters.eq(id)).toOption()
    }.mapLeft { GlobalException.DatabaseProblem(it) }
        .flatMap { todoOptionToEither(it, id) }

    private val todoOptionToEither: suspend (todoOption: Option<Todo>, id: ObjectId) -> Either<GlobalException, Todo> =
        { todoOptional, id -> todoOptional.toEither { GlobalException.NotFoundId(id) } }

    val updateTodo: suspend (todo: Todo) -> Either<GlobalException, Todo> = {
        Either.catch{
            it.update<Todo>().awaitSuspending()
        }.mapLeft { GlobalException.DatabaseProblem(it) }
    }

    suspend fun findPagesByFilter(
        pageReq: PageRequest,
        searchKey: String,
        startDate: Instant,
        endDate: Instant
    ): Either<GlobalException.DatabaseProblem, List<Todo>> = Either.catch {
        this.find(
            "${Todo::title.name} like ?1"
                    + "and ${Todo::createdTime.name} >= ?2 and ${Todo::createdTime.name} <= ?3",
            sort,
            searchKey, startDate, endDate
        )
            .page(pageReq.page - 1, pageReq.show).list()
            .awaitSuspending()
    }.mapLeft { GlobalException.DatabaseProblem(it) }

    suspend fun findByFilter(searchKey: String): Either<GlobalException.DatabaseProblem, List<Todo>> = Either.catch{
        this.find(
            "${Todo::title.name} like ?1",
            sort,
            searchKey
        ).list().awaitSuspending()
    }.mapLeft { GlobalException.DatabaseProblem(it) }
}
