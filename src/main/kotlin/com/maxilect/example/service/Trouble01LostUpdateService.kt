package com.maxilect.example.service

import jakarta.persistence.LockModeType
import mu.KLogging
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CyclicBarrier

@Service
class Trouble01LostUpdateService(
    private val transactionTemplate: TransactionTemplate
) : AbstractService() {

    @Transactional
    fun increaseValue(name: String, entityId: Long, increment: Long) {
        var entity = findEntityById(entityId)

        Thread.sleep(500)

        entity.value += increment
        entity = testRepository.saveAndFlush(entity)
        logger.info { "$name Save entity with value = ${entity.value}" }

        Thread.sleep(500)

        logger.info { "$name Commit" }
    }

    @Transactional
    fun increaseUpdateValue(barrier: CyclicBarrier, name: String, entityId: Long, increment: Long) {
        logger.info { "Job $name run and wait" }
        barrier.await()
        logger.info { "Job $name executed" }

        testRepository.incrementUpdate(entityId, increment)
        testRepository.flush()
        logger.info { "$name Increment entity value" }

        clear()

        val entity = findEntityById(entityId)
        logger.info { "$name Read entity with value = ${entity.value}" }
    }

    companion object : KLogging()

}