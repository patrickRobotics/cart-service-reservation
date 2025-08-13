package com.example.promoquoter.promotion

import com.example.promoquoter.domain.CustomerSegment
import com.example.promoquoter.domain.Product
import java.math.BigDecimal
import java.util.*


data class PromotionContext(
    val customerSegment: CustomerSegment,
    val cartItems: List<CartLineItem>,
    val appliedPromotions: MutableList<AppliedPromotion> = mutableListOf()
)


data class CartLineItem(
    val product: Product,
    var quantity: Int,
    var subtotal: BigDecimal,
    var discount: BigDecimal = BigDecimal.ZERO
) {
    val finalPrice: BigDecimal
        get() = subtotal - discount
}


data class AppliedPromotion(
    val promotionId: UUID,
    val type: String,
    val description: String,
    val discountAmount: BigDecimal
)