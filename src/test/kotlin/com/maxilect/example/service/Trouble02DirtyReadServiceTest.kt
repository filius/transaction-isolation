package com.maxilect.example.service

import com.maxilect.example.configuration.AbstractSpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CompletableFuture

class Trouble02DirtyReadServiceTest : AbstractSpringTest() {

    @Autowired
    private lateinit var trouble02DirtyReadService: Trouble02DirtyReadService

    private var entityId: Long = -1

    @BeforeEach
    fun beforeEach() {
        testRepository.deleteAll()
        entityId = createEntity(value = 10).id
    }

    @Test
    fun test() {
        val future1 = CompletableFuture.supplyAsync {
            trouble02DirtyReadService.dirtyReadTransaction(
                entityId = entityId
            )
        }

        val future2 = CompletableFuture.runAsync {
            try {
                trouble02DirtyReadService.rollbackTransaction(
                    entityId = entityId,
                    newValue = 20
                )
            } catch (_: Exception) {
                // swallow exception
            }
        }

        val dirtyReadResult = future1.get()
        future2.get()

        val actual = getEntityById(entityId)

        logger.info { "Read entity with value = ${actual.value}" }

        assertThat(actual.value).isEqualTo(10) // commited value
        assertThat(dirtyReadResult.first).isNotEqualTo(dirtyReadResult.second) // compare first and second read values
    }
}