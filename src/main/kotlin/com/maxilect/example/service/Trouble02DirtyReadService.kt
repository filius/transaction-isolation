package com.maxilect.example.service

import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

// Transaction 1                                    |  Transaction 2
// SELECT value FROM test_table WHERE id=1; -- (10) |
//                                                  |  UPDATE test_table SET value=20 WHERE id=1;
// SELECT value FROM test_table WHERE id=1; -- (20) |
//                                                  |  ROLLBACK;

@Service
class Trouble02DirtyReadService : AbstractService() {

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    fun dirtyReadTransaction(entityId: Long) : Pair<Long, Long> {
        val entity1 = findEntityById(entityId)
        logger.info { "TX1 First read entity with value = ${entity1.value}" }
        detach(entity1)

        Thread.sleep(1000)

        val entity2 = findEntityById(entityId)
        logger.info { "TX1 Second read entity with value = ${entity2.value}" }

        return Pair(entity1.value, entity2.value)
    }

    @Transactional
    fun rollbackTransaction(entityId: Long, newValue: Long) {
        var entity = findEntityById(entityId)
        entity.value = newValue
        entity = testRepository.saveAndFlush(entity)
        logger.info { "TX2 Save entity with value = ${entity.value}" }

        Thread.sleep(1000)

        logger.info { "TX2 rolling back" }
        error("Forced error in TX2")
    }

    companion object : KLogging()

}