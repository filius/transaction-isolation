package com.maxilect.example.service

import com.maxilect.example.entity.TestEntity
import com.maxilect.example.repository.TestRepository
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractService {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var testRepository: TestRepository

    protected fun findEntityById(entityId: Long) : TestEntity =
        testRepository.findById(entityId).orElseThrow { error("Entity not found") }

    protected fun detach(entity: TestEntity) =
        entityManager.detach(entity)

    protected fun clear() =
        entityManager.clear()
}