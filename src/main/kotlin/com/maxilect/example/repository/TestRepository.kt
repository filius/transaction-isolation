package com.maxilect.example.repository

import com.maxilect.example.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TestRepository : JpaRepository<TestEntity, Long> {

    @Modifying
    @Query("UPDATE TestEntity SET value = :newValue WHERE id = :entityId")
    fun phantomReadUpdate(entityId: Long, newValue: Long)

    @Modifying
    @Query("UPDATE TestEntity SET value = value + :incValue WHERE id = :entityId")
    fun incrementUpdate(entityId: Long, incValue: Long)
}