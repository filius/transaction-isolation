package com.maxilect.example.service

import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

// Transaction 1                                    |  Transaction 2
// SELECT value FROM test_table WHERE id=1; -- (10) |
//                                                  |  UPDATE test_table SET value=20 WHERE id=1;
//                                                  |  COMMIT;
// SELECT value FROM test_table WHERE id=1; -- (20) |

@Service
class Trouble03NonRepeatableReadService : AbstractService() {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun nonRepeatableReadTransaction(entityId: Long) : Pair<Long, Long> {
        val entity1 = findEntityById(entityId)
        logger.info { "TX1 First read entity with value = ${entity1.value}" }
        detach(entity1)

        Thread.sleep(1000)

        val entity2 = findEntityById(entityId)
        logger.info { "TX1 Second read entity with value = ${entity2.value}" }

        return Pair(entity1.value, entity2.value)
    }

    @Transactional
    fun updateTransaction(entityId: Long, newValue: Long) {
        var entity = findEntityById(entityId)
        entity.value = newValue
        entity = testRepository.saveAndFlush(entity)
        logger.info { "TX2 Save entity with value = ${entity.value}" }

        logger.info { "TX2 Commit" }
    }

    companion object : KLogging()

}