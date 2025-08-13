package com.example.promoquoter.repository

import com.example.promoquoter.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): Optional<Order>
}