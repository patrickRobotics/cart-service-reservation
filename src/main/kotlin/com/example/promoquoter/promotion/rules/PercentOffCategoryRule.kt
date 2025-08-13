package com.example.promoquoter.promotion.rules

import com.example.promoquoter.domain.Promotion
import com.example.promoquoter.promotion.AppliedPromotion
import com.example.promoquoter.promotion.PromotionContext
import com.example.promoquoter.promotion.PromotionRule
import java.math.BigDecimal
import java.math.RoundingMode


class PercentOffCategoryRule(private val promotion: Promotion) : PromotionRule {
    override val priority: Int = promotion.priority
    override val description: String =
        "${(promotion.discountPercentage!! * BigDecimal(100)).toInt()}% off ${promotion.targetCategory} category"

    override fun canApply(context: PromotionContext): Boolean {
        return promotion.targetCategory != null &&
                promotion.discountPercentage != null &&
                context.cartItems.any { it.product.category == promotion.targetCategory }
    }

    override fun apply(context: PromotionContext): PromotionContext {
        var totalDiscount = BigDecimal.ZERO

        context.cartItems
            .filter { it.product.category == promotion.targetCategory }
            .forEach { item ->
                val itemDiscount = item.subtotal
                    .multiply(promotion.discountPercentage!!)
                    .setScale(2, RoundingMode.HALF_UP)

                item.discount = item.discount.add(itemDiscount)
                totalDiscount = totalDiscount.add(itemDiscount)
            }

        if (totalDiscount > BigDecimal.ZERO) {
            context.appliedPromotions.add(
                AppliedPromotion(
                    promotionId = promotion.id!!,
                    type = promotion.type.name,
                    description = description,
                    discountAmount = totalDiscount
                )
            )
        }

        return context
    }
}