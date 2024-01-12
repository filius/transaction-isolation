package com.maxilect.example.service

import com.maxilect.example.configuration.AbstractSpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CompletableFuture

class Trouble03NonRepeatableReadServiceTest : AbstractSpringTest() {

    @Autowired
    private lateinit var trouble03NonRepeatableReadService: Trouble03NonRepeatableReadService

    private var entityId: Long = -1

    @BeforeEach
    fun beforeEach() {
        testRepository.deleteAll()
        entityId = createEntity(value = 10).id
    }

    @Test
    fun test() {
        val future1 = CompletableFuture.supplyAsync {
            trouble03NonRepeatableReadService.nonRepeatableReadTransaction(
                entityId = entityId
            )
        }

        val future2 = CompletableFuture.runAsync {
            Thread.sleep(500)

            trouble03NonRepeatableReadService.updateTransaction(
                entityId = entityId,
                newValue = 20
            )
        }

        val nonRepeatableResult = future1.get()
        future2.get()

        val actual = getEntityById(entityId)

        logger.info { "Read entity with value = ${actual.value}" }

        assertThat(actual.value).isEqualTo(20) // commited value
        assertThat(nonRepeatableResult.first).isNotEqualTo(nonRepeatableResult.second) // compare first and second read values
    }
}