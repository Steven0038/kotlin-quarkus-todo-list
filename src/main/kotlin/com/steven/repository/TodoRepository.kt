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
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import java.util.*
import javax.inject.Singleton


@Singleton
class TodoRepository : BaseMongoRepository<Todo>() {

    val create: suspend (task: Todo) -> Either<GlobalException, Todo> = { task ->
        Either.catch {
            task.save()
        }.mapLeft { GlobalException.DatabaseProblem(it) }
    }

    suspend fun findByObjId(id: ObjectId): Either<GlobalException, Todo> = Either.catch {
        this.findOne(Filters.eq(id)).toOption()
    }.mapLeft { GlobalException.DatabaseProblem(it) }
        .flatMap { fruitOptionToEither(it, id) }

    private val fruitOptionToEither: suspend (maybeFruit: Option<Todo>, id: ObjectId) -> Either<GlobalException, Todo> =
        { taskOptional, id -> taskOptional.toEither { GlobalException.NotFoundId(id) } }

    suspend fun findOneByIdAndUpdate(todo: Todo): Todo? {
        return col.findOneAndUpdate(
            Filters.eq(todo.id),
            Updates.combine(
                Updates.set(Todo::lastModifiedTime.name, Date()),
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }
}
