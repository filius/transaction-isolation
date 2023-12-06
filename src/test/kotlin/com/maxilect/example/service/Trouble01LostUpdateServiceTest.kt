package com.maxilect.example.service

import com.maxilect.example.configuration.AbstractSpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier


class Trouble01LostUpdateServiceTest : AbstractSpringTest() {

    @Autowired
    private lateinit var trouble01LostUpdateService : Trouble01LostUpdateService

    private var entityId: Long = -1

    @BeforeEach
    fun beforeEach() {
        testRepository.deleteAll()
        entityId = createEntity(value = 10).id
    }

    @Test
    fun test() {
        val future1 = CompletableFuture.runAsync {
            trouble01LostUpdateService.increaseValue(
                name = "TX1",
                entityId = entityId,
                increment = 20
            )
        }

        val future2 = CompletableFuture.runAsync {
            trouble01LostUpdateService.increaseValue(
                name = "TX2",
                entityId = entityId,
                increment = 30
            )
        }

        future1.get()
        future2.get()

        val actual = getEntityById(entityId)
        logger.info { "Read entity with value = ${actual.value}" }

        assertThat(actual.value).isEqualTo(40) // not 60
    }

    @Test
    fun test2() {
        val barrier = CyclicBarrier(3)

        val future1 = CompletableFuture.runAsync {
            trouble01LostUpdateService.increaseUpdateValue(
                barrier = barrier,
                name = "TX1",
                entityId = entityId,
                increment = 20
            )
        }

        val future2 = CompletableFuture.runAsync {
            trouble01LostUpdateService.increaseUpdateValue(
                barrier = barrier,
                name = "TX2",
                entityId = entityId,
                increment = 30
            )
        }

        Thread.sleep(20)

        barrier.await()

        future1.get()
        future2.get()

        val actual = getEntityById(entityId)
        logger.info { "Read entity with value = ${actual.value}" }

        assertThat(actual.value).isEqualTo(40) // not 60
    }
}