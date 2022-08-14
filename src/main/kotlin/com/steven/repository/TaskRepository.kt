package com.steven.repository

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.toOption
import com.mongodb.client.model.*
import com.steven.exception.GlobalException
import com.steven.model.po.task.Task
import com.steven.model.po.task.TaskState
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import java.util.*
import javax.inject.Singleton


@Singleton
class TaskRepository : BaseMongoRepository<Task>() {

//    init {
//        createIndexes(
//            IndexModel(Indexes.ascending(Task::userId.name))
//        )
//    }

    val create: suspend (task: Task) -> Either<GlobalException, Task> = { task ->
        Either.catch {
            task.save()
        }.mapLeft { GlobalException.DatabaseProblem(it) }
    }

    suspend fun findByObjId(id: ObjectId): Either<GlobalException, Task> = Either.catch {
        this.findOne(Filters.eq(id)).toOption()
    }.mapLeft { GlobalException.DatabaseProblem(it) }
        .flatMap { fruitOptionToEither(it, id) }

    private val fruitOptionToEither: suspend (maybeFruit: Option<Task>, id: ObjectId) -> Either<GlobalException, Task> =
        { taskOptional, id -> taskOptional.toEither { GlobalException.NotFoundId(id) } }

    suspend fun findOneByIdAndUpdateState(task: Task, newState: TaskState, userId: ObjectId): Task? {
        return col.findOneAndUpdate(
            Filters.eq(task.id),
            Updates.combine(
                Updates.set(Task::state.name, newState.value),
                Updates.set(Task::userId.name, userId),
                Updates.set(Task::lastModifiedTime.name, Date()),
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

}
