package com.example.promoquoter.dto

import com.example.promoquoter.domain.ProductCategory
import com.example.promoquoter.domain.PromotionType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.util.*


data class CreatePromotionRequest(
    val type: PromotionType,
    val targetCategory: ProductCategory? = null,
    val targetProductId: UUID? = null,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val discountPercentage: BigDecimal? = null,

    @field:Min(1)
    val buyQuantity: Int? = null,

    @field:Min(1)
    val getQuantity: Int? = null,

    @field:Min(0)
    val priority: Int = 100
)

data class UpdatePromotionRequest(
    val type: PromotionType? = null,
    val targetCategory: ProductCategory? = null,
    val targetProductId: UUID? = null,
    val discountPercentage: BigDecimal? = null,
    val buyQuantity: Int? = null,
    val getQuantity: Int? = null,
    val priority: Int? = null
)

data class PromotionResponse(
    val id: UUID,
    val type: PromotionType,
    val targetCategory: ProductCategory?,
    val targetProductId: UUID?,
    val discountPercentage: BigDecimal?,
    val buyQuantity: Int?,
    val getQuantity: Int?,
    val priority: Int,
    val active: Boolean
)