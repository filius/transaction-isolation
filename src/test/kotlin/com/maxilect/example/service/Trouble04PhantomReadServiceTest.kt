package com.maxilect.example.service

import com.maxilect.example.configuration.AbstractSpringTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CompletableFuture

class Trouble04PhantomReadServiceTest : AbstractSpringTest() {

    @Autowired
    private lateinit var trouble04PhantomReadService: Trouble04PhantomReadService

    private var entityId: Long = -1

    @BeforeEach
    fun beforeEach() {
        testRepository.deleteAll()
        entityId = createEntity(value = 10).id
    }

    @Test
    fun test() {
        val future1 = CompletableFuture.supplyAsync {
            trouble04PhantomReadService.phantomReadTransaction(entityId, 30)
        }

        val future2 = CompletableFuture.runAsync {
            Thread.sleep(500)
            trouble04PhantomReadService.insertTransaction(
                value = 20
            )
        }

        val phantomResult = future1.get()
        future2.get()

        assertSoftly { softly ->
            softly.assertThat(phantomResult.first).isEqualTo(phantomResult.second)
            softly.assertThat(phantomResult.first).isEqualTo(phantomResult.third)
        }
    }
}