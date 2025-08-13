package com.example.promoquoter.domain

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.util.*


@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @field:NotBlank
    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ProductCategory,

    @field:DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @field:Min(0)
    @Column(nullable = false)
    val stock: Int,

    @Version
    val version: Long = 0
) {
    constructor() : this(null, "", ProductCategory.GENERAL, BigDecimal.ZERO, 0, 0)
}


enum class ProductCategory {
    ELECTRONICS, CLOTHING, BOOKS, GENERAL, FOOD, SPORTS
}
