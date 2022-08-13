package com.steven.repository

import com.mongodb.client.model.*
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
