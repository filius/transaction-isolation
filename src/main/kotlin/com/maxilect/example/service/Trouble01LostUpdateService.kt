package com.maxilect.example.service

import mu.KLogging
import org.hibernate.Session
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Connection
import java.util.concurrent.CyclicBarrier

// Transaction 1                                        |  Transaction 2
// UPDATE test_table SET value = value + 20 WHERE id=1; |  UPDATE test_table SET value = value + 20 WHERE id=1;

@Service
class Trouble01LostUpdateService : AbstractService() {

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

    fun increaseUpdateValue(barrier: CyclicBarrier, name: String, entityId: Long, increment: Long) {
        val session: Session = entityManager.unwrap(Session::class.java)
        logger.info { "$name get session" }
        session.use { ses ->
            ses.doWork {
                it.use { con ->
                    con.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED
                    con.autoCommit = false
                    val stmt = con.prepareStatement(
                        "UPDATE test_table SET value = value + $increment WHERE id = $entityId"
                    )
                    logger.info { "$name wait barrier" }
                    barrier.await()
                    logger.info { "$name execute begin" }
                    stmt.executeUpdate()
                    logger.info { "$name execute end" }
                    con.commit()
                    logger.info { "$name commit" }
                }
            }
        }
    }

    companion object : KLogging()

}