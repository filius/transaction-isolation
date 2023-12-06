package com.maxilect.example.service

import com.maxilect.example.entity.TestEntity
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

// Transaction 1                                                        |  Transaction 2
// SELECT value FROM test_table WHERE id>0; -- (10)                     |
//                                                                      |  INSERT INTO test_table VALUES (2, 20);
//                                                                      |  COMMIT;
// SELECT value FROM test_table WHERE id>0; -- (10) !! doesn't occurred |
// UPDATE test_table SET value 30 WHERE id=2;                           |
// SELECT value FROM test_table WHERE id>1; -- (10, 30) !! occurred     |

@Service
class Trouble04PhantomReadService : AbstractService() {

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun phantomReadTransaction(entityId: Long, newValue: Long): Triple<Set<Long>, Set<Long>, Set<Long>> {
        val entityList1 = testRepository.findAll()
        logger.info { "TX1 First read entity list: $entityList1" }
        clear()

        Thread.sleep(1000)

        val entityList2 = testRepository.findAll()
        logger.info { "TX1 Second read entity list: $entityList2" }

        // Consistent Nonlocking Read (snapshot usage) - https://dev.mysql.com/doc/refman/8.0/en/innodb-consistent-read.html
        testRepository.phantomReadUpdate(entityId + 1, newValue)
        logger.info { "TX1 Update new record" }

        val entityList3 = testRepository.findAll()
        logger.info { "TX1 Third read entity list: $entityList3" }

        return Triple(
            entityList1.map { it.value }.toSet(),
            entityList2.map { it.value }.toSet(),
            entityList3.map { it.value }.toSet()
        )
    }

    @Transactional
    fun insertTransaction(value: Long) {
        val entity = testRepository.saveAndFlush(
            TestEntity(
                value = value
            )
        )
        logger.info { "TX2 Insert entity with value = ${entity.value}" }

        logger.info { "TX2 Commit" }
    }

    companion object : KLogging()

}