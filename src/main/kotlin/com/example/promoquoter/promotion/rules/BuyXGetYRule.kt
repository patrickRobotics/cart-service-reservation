package com.example.promoquoter.promotion.rules

import com.example.promoquoter.domain.Promotion
import com.example.promoquoter.promotion.AppliedPromotion
import com.example.promoquoter.promotion.PromotionContext
import com.example.promoquoter.promotion.PromotionEngine
import com.example.promoquoter.promotion.PromotionRule
import org.slf4j.LoggerFactory
import java.math.BigDecimal


class BuyXGetYRule(
    private val promotion: Promotion
) : PromotionRule {
    private val logger = LoggerFactory.getLogger(BuyXGetYRule::class.java)

    override val priority: Int = promotion.priority
    override val description: String =
        "Buy ${promotion.buyQuantity} get ${promotion.getQuantity} free"

    override fun canApply(context: PromotionContext): Boolean {
        return promotion.targetProductId != null &&
                promotion.buyQuantity != null &&
                promotion.getQuantity != null &&
                context.cartItems.any {
                    it.product.id == promotion.targetProductId && it.quantity >= promotion.buyQuantity!!
                }
    }

    override fun apply(context: PromotionContext): PromotionContext {
        val targetItem = context.cartItems.find { it.product.id == promotion.targetProductId }
            ?: return context

        val buyQty = promotion.buyQuantity!!
        val getQty = promotion.getQuantity!!

        val promotionSets = targetItem.quantity / (buyQty + getQty)
        val freeItems = promotionSets * getQty

        if (freeItems > 0) {
            val unitPrice = targetItem.product.price
            val discount = unitPrice.multiply(BigDecimal(freeItems))

            targetItem.discount = targetItem.discount.add(discount)

            context.appliedPromotions.add(
                AppliedPromotion(
                    promotionId = promotion.id!!,
                    type = promotion.type.name,
                    description = "$description (${freeItems} free items)",
                    discountAmount = discount
                )
            )
        }

        return context
    }
}