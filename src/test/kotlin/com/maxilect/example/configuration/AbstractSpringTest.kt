package com.maxilect.example.configuration

import com.maxilect.example.entity.TestEntity
import com.maxilect.example.repository.TestRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ActiveProfiles("test")
@SpringBootTest(classes = [TestConfiguration::class])
@ContextConfiguration(initializers = [TestContainersInitializer::class])
abstract class AbstractSpringTest {

    @Autowired
    protected lateinit var testRepository: TestRepository

    protected fun getEntityById(entityId: Long): TestEntity =
        testRepository.findById(entityId).orElseThrow { error("Entity not found") }

    protected fun createEntity(
        value: Long = 10
    ): TestEntity =
        testRepository.save(
            TestEntity(value = value)
        )

    companion object : KLogging()
}