package com.example.promoquoter.dto

import com.example.promoquoter.domain.ProductCategory
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*


data class CreateProductRequest(
    @field:NotBlank(message = "Product name cannot be blank")
    @field:JsonProperty("name")
    val name: String,

    @field:NotNull(message = "Product category is required")
    @field:JsonProperty("category")
    val category: ProductCategory,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @field:JsonProperty("price")
    val price: BigDecimal,

    @field:NotNull(message = "Stock is required")
    @field:Min(value = 0, message = "Stock cannot be negative")
    @field:JsonProperty("stock")
    val stock: Int
)


data class ProductResponse(
    val id: UUID,
    val name: String,
    val category: ProductCategory,
    val price: BigDecimal,
    val stock: Int
)