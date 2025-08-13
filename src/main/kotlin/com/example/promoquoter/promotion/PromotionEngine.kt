package com.example.promoquoter.promotion

import com.example.promoquoter.domain.Promotion
import com.example.promoquoter.promotion.rules.BuyXGetYRule
import com.example.promoquoter.promotion.rules.PercentOffCategoryRule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class PromotionEngine {
    private val logger = LoggerFactory.getLogger(PromotionEngine::class.java)

    fun applyPromotions(context: PromotionContext, availablePromotions: List<Promotion>): PromotionContext {
        logger.info("Checking for active promotions to apply to the cart items")
        val rules = availablePromotions.map { createRule(it) }.sortedBy { it.priority }
        logger.info("Available promotion rules: $rules")

        return rules.fold(context) { currentContext, rule ->
            if (rule.canApply(currentContext)) {
                rule.apply(currentContext)
            } else {
                currentContext
            }
        }
    }

    private fun createRule(promotion: Promotion): PromotionRule {
        return when (promotion.type) {

            // Return any applicable promotion for calculating discounts

            com.example.promoquoter.domain.PromotionType.PERCENT_OFF_CATEGORY ->
                PercentOffCategoryRule(promotion)
            com.example.promoquoter.domain.PromotionType.BUY_X_GET_Y ->
                BuyXGetYRule(promotion)
            else -> throw IllegalArgumentException("Unsupported promotion type: ${promotion.type}")
        }
    }
}