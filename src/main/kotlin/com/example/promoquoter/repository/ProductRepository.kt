package com.example.promoquoter.repository

import com.example.promoquoter.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import jakarta.persistence.LockModeType
import java.util.*


@Repository
interface ProductRepository : JpaRepository<Product, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    fun findByIdWithLock(id: UUID): Optional<Product>

    fun findAllByIdIn(ids: List<UUID>): List<Product>
}