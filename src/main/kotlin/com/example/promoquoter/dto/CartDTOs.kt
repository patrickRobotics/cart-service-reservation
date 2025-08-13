package com.example.promoquoter.dto

import com.example.promoquoter.domain.CustomerSegment
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*


data class CartQuoteRequest(
    @field:Valid
    @field:NotEmpty(message = "Provide list of cart items")
    val items: List<CartItemRequest>,

    @field:NotNull(message = "CustomerSegment is required")
    val customerSegment: CustomerSegment
)


data class CartItemRequest(
    @field:NotNull(message = "productId is required")
    val productId: UUID,

    @field:Min(1)
    val qty: Int
)


data class CartQuoteResponse(
    val items: List<LineItemResponse>,
    val appliedPromotions: List<AppliedPromotionResponse>,
    val subtotal: BigDecimal,
    val totalDiscount: BigDecimal,
    val finalTotal: BigDecimal
)


data class LineItemResponse(
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineSubtotal: BigDecimal,
    val lineDiscount: BigDecimal,
    val lineFinal: BigDecimal
)


data class AppliedPromotionResponse(
    val promotionId: UUID,
    val type: String,
    val description: String,
    val discountAmount: BigDecimal
)


data class CartConfirmResponse(
    val orderId: UUID,
    val items: List<LineItemResponse>,
    val appliedPromotions: List<AppliedPromotionResponse>,
    val subtotal: BigDecimal,
    val totalDiscount: BigDecimal,
    val finalTotal: BigDecimal
)