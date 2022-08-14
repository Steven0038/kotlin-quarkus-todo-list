package com.steven

import com.steven.repository.TaskRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskResourceTestTest {

    @Inject
    lateinit var taskRepo: TaskRepository

    @Test
    fun `test add tasks`() {
        Given {
            body("""{"title" : "todo task1","description":"swimming with friends", "startDate":"1642348800000", "deprecateDate":"1642348800000"}""")
            contentType("application/json")
        } When {
            post("/api/task/create")
        } Then {
            statusCode(201)
            body(
                CoreMatchers.containsString(""""id":"""),
                CoreMatchers.containsString(""""title":"todo task1"""")
            )
        }

        val uid: String = Given {
            body(
                """
                {"title" : "todo task2",
                "description":"watch movies", 
                "startDate":"1642348800000", 
                "deprecateDate":"1642348800000"}
                """.trimIndent()
            )
                .contentType("application/json")
        } When {
            post("/api/task/create")
        } Then {
            statusCode(201)
            body(CoreMatchers.containsString(""""id":"""), CoreMatchers.containsString(""""title":"todo task2""""))
        } Extract {
            path("id")
        }

        println("get uid : $uid")

        Given {
            log().method()
        } When {
            get("/api/task/$uid")
        } Then {
            statusCode(200)
            body(CoreMatchers.containsString(uid), CoreMatchers.containsString(""""title":"todo task2""""))
        }

        Given {
            log().method()
        } When {
            get("/api/task/list?keyword=todo task&page=1&show=10&since=1640966400000&until=32472115200000")
        } Then {
            statusCode(200)
            body(
                CoreMatchers.containsString("todo task1"),
                CoreMatchers.containsString("todo task2")
            )
        }

        Given {
            body(
                """
                {
                "id": "$uid",
                "title" : "todo task2",
                "description":"not watch movies", 
                "startDate":"1642348800000", 
                "deprecateDate":"1642348800000"}
                """.trimIndent()
            )
                .contentType("application/json")
        } When {
            put("/api/task/$uid/update")
        } Then {
            statusCode(200)
            body(
                CoreMatchers.containsString("not watch movies")
            )
        }


    }

    @Test
    fun `list user task details`() {
        runBlocking {
            val task = taskRepo.findAll().firstResult().awaitSuspending()
            Assertions.assertNotNull(task)
        }
    }

//    @AfterAll
//    fun clean() {
//        runBlocking {
//            taskRepo.mongoCollection().drop().awaitSuspending()
//        }
//    }
}
