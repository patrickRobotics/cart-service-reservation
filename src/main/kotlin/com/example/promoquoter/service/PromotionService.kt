package com.example.promoquoter.service

import com.example.promoquoter.domain.Promotion
import com.example.promoquoter.dto.CreatePromotionRequest
import com.example.promoquoter.dto.PromotionResponse
import com.example.promoquoter.repository.PromotionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class PromotionService(private val promotionRepository: PromotionRepository) {

    fun createPromotions(requests: List<CreatePromotionRequest>): List<PromotionResponse> {
        val promotions = requests.map { request ->
            validatePromotionRequest(request)
            Promotion(
                type = request.type,
                targetCategory = request.targetCategory,
                targetProductId = request.targetProductId,
                discountPercentage = request.discountPercentage,
                buyQuantity = request.buyQuantity,
                getQuantity = request.getQuantity,
                priority = request.priority
            )
        }

        val savedPromotions = promotionRepository.saveAll(promotions)

        return savedPromotions.map { promotion ->
            PromotionResponse(
                id = promotion.id!!,
                type = promotion.type,
                targetCategory = promotion.targetCategory,
                targetProductId = promotion.targetProductId,
                discountPercentage = promotion.discountPercentage,
                buyQuantity = promotion.buyQuantity,
                getQuantity = promotion.getQuantity,
                priority = promotion.priority,
                active = promotion.active
            )
        }
    }

    private fun validatePromotionRequest(request: CreatePromotionRequest) {
        when (request.type) {
            com.example.promoquoter.domain.PromotionType.PERCENT_OFF_CATEGORY -> {
                require(request.targetCategory != null) { "Target category is required for PERCENT_OFF_CATEGORY" }
                require(request.discountPercentage != null) { "Discount percentage is required for PERCENT_OFF_CATEGORY" }
            }
            com.example.promoquoter.domain.PromotionType.BUY_X_GET_Y -> {
                require(request.targetProductId != null) { "Target product ID is required for BUY_X_GET_Y" }
                require(request.buyQuantity != null) { "Buy quantity is required for BUY_X_GET_Y" }
                require(request.getQuantity != null) { "Get quantity is required for BUY_X_GET_Y" }
            }
            else -> throw IllegalArgumentException("Unsupported promotion type: ${request.type}")
        }
    }
}