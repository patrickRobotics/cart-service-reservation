package com.example.promoquoter.domain

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.util.*


@Entity
@Table(name = "promotions")
data class Promotion(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PromotionType,

    @Column(name = "target_category")
    @Enumerated(EnumType.STRING)
    val targetCategory: ProductCategory? = null,

    @Column(name = "target_product_id")
    val targetProductId: UUID? = null,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    @Column(name = "discount_percentage", precision = 5, scale = 4)
    val discountPercentage: BigDecimal? = null,

    @field:Min(1)
    @Column(name = "buy_quantity")
    val buyQuantity: Int? = null,

    @field:Min(1)
    @Column(name = "get_quantity")
    val getQuantity: Int? = null,

    @field:Min(0)
    @Column(nullable = false)
    val priority: Int = 100,

    @Column(nullable = false)
    val active: Boolean = true
) {
    constructor() : this(null, PromotionType.PERCENT_OFF_CATEGORY, null, null, null, null, null, 100, true)
}


enum class PromotionType {
    PERCENT_OFF_CATEGORY, BUY_X_GET_Y, BULK_DISCOUNT
}