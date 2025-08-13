package com.example.promoquoter.repository

import com.example.promoquoter.domain.Promotion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface PromotionRepository : JpaRepository<Promotion, UUID> {
    fun findByActiveOrderByPriority(active: Boolean): List<Promotion>
}