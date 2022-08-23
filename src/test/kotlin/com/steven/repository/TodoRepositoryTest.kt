package com.steven.repository

import com.steven.Todo
import com.steven.exception.GlobalException
import com.steven.model.dto.PageRequest
import io.kotest.matchers.date.shouldBeBetween
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.time.Instant
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TodoRepositoryTest {

    @Inject
    private lateinit var todoDao: TodoRepository

    @BeforeEach
    fun init() {
        runBlocking {
            Todo().apply {
                this.title = "test title"
                this.completed = false
                this.priority = 1
            }.persist<Todo>().awaitSuspending()
        }
    }

    @Test
    fun `create todo`() {
        runBlocking {
            val todoCreate = Todo().apply {
                this.title = "new test title"
                this.completed = true
                this.priority = 2
            }
            val resultEither = todoCreate.run { todoDao.create(this) }
            resultEither.map { Assertions.assertEquals(it.title, todoCreate.title) }
            resultEither.fold( ifRight = { it.title.shouldBe(todoCreate.title) }, ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() })
            resultEither.fold( ifRight = { it.title.shouldBe(todoCreate.completed) }, ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() })
            resultEither.fold( ifRight = { it.title.shouldBe(todoCreate.priority) }, ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() })
        }
    }


    @Test
    fun `find by obj id `() {
        runBlocking {
            val todo = todoDao.findAll().firstResult().awaitSuspending()
            todo?.id
                ?.run { todoDao.findByObjId(this) }
                ?.fold(
                    ifRight = { it.id.shouldBe(todo.id) },
                    ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() }
                )
                ?: throw AssertionError()
        }
    }

    @Test
    fun `todo option to either`() {
        // TODO
    }

    @Test
    fun `find by id and update`() {
        runBlocking {
            val todo = todoDao.findAll().firstResult().awaitSuspending()
            val tobeUpdate = todo?.apply {
                this.title = "update test title"
                this.completed = true
                this.priority = 2
            }

            val result = tobeUpdate?.apply { todoDao.updateTodo(this) }

            Assertions.assertEquals(result?.title, tobeUpdate?.title)
            Assertions.assertEquals(result?.completed, tobeUpdate?.completed)
            Assertions.assertEquals(result?.priority, tobeUpdate?.priority)
        }
    }

    @Test
    fun `update todo`() {
        runBlocking {
            val todo = todoDao.findAll().firstResult().awaitSuspending()
            val tobeUpdate = todo?.apply {
                this.title = "update test title"
                this.completed = true
                this.priority = 2
            }

            tobeUpdate?.run { todoDao.updateTodo(this) }
                ?.fold(
                    ifRight = {
                        it.id.shouldBe(tobeUpdate.id)
                        it.title.shouldBe(tobeUpdate.title)
                        it.completed.shouldBe(tobeUpdate.completed)
                        it.priority.shouldBe(tobeUpdate.priority)
                    },
                    ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() }
                )
                ?: throw AssertionError()
        }
    }

    @Test
    fun `find pages by filter`() {
        runBlocking {
            val req = PageRequest().apply {
                this.page = 1
                this.show = 10
            }

            val todo = todoDao.findAll().firstResult().awaitSuspending() ?: throw AssertionError("list hava no record")
            val filter = todo.title ?: throw AssertionError("no title data")
            val startDate = todo.createdTime.minusMillis(123456)
            val endDate = Instant.ofEpochMilli(32472115200000)

            todoDao.findPagesByFilter(req, filter, startDate, endDate).fold(
                ifRight = {
                    it.size.shouldBeLessThan(10)
                    it.first().title.shouldContain(filter)
                    it.first().createdTime.shouldBeBetween(startDate, endDate)
                },
                ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() }
            )
        }
    }

    @Test
    fun `find by filter`() {
        runBlocking {
            val todo = todoDao.findAll().firstResult().awaitSuspending() ?: throw AssertionError("list hava no record")
            val filter = todo.title ?: throw AssertionError("no title data")

            todoDao.findByFilter(filter).fold(
                ifRight = {
                          it.first().title.shouldContain(filter)
                },
                ifLeft = { it.shouldBeInstanceOf<GlobalException.DatabaseProblem>() }
            )
        }
    }


    @AfterAll
    fun clean() {
        runBlocking {
            todoDao.mongoCollection().drop().awaitSuspending()
        }
    }

}