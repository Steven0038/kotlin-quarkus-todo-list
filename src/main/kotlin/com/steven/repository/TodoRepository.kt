package com.steven.repository

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.toOption
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.steven.Todo
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*
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
        .flatMap { fruitOptionToEither(it, id) }

    private val fruitOptionToEither: suspend (todoOption: Option<Todo>, id: ObjectId) -> Either<GlobalException, Todo> =
        { todoOptional, id -> todoOptional.toEither { GlobalException.NotFoundId(id) } }

    suspend fun findOneByIdAndUpdate(todo: Todo): Todo? {
        return col.findOneAndUpdate(
            Filters.eq(todo.id),
            Updates.combine(
                Updates.set(Todo::lastModifiedTime.name, Date()),
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    suspend fun findPagesByFilter(pageReq: PageRequest, searchKey: String, startDate: Instant, endDate: Instant): List<Todo> {
        return find(
            "${Todo::title.name} like ?1"
                    + "and ${Todo::createdTime.name} >= ?2 and ${Todo::createdTime.name} <= ?3",
            sort,
            searchKey, startDate, endDate
        )
            .page(pageReq.page - 1, pageReq.show).list()
            .awaitSuspending()
    }

    suspend fun findByFilter(searchKey: String): List<Todo> {
        return find(
            "${Todo::title.name} like ?1",
            sort,
            searchKey
        ).list().awaitSuspending()
    }
}
