package com.example.promoquoter.promotion

import com.example.promoquoter.domain.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@SpringBootTest
class PromotionEngineTest {

    private val promotionEngine = PromotionEngine()

    @Test
    fun `should apply percent off category promotion`() {
        // Given
        val product = Product(
            id = UUID.randomUUID(),
            name = "Electronics Item",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("100.00"),
            stock = 10
        )

        val cartItem = CartLineItem(
            product = product,
            quantity = 2,
            subtotal = BigDecimal("200.00")
        )

        val promotion = Promotion(
            id = UUID.randomUUID(),
            type = PromotionType.PERCENT_OFF_CATEGORY,
            targetCategory = ProductCategory.ELECTRONICS,
            discountPercentage = BigDecimal("0.1"), // 10%
            priority = 1
        )

        val context = PromotionContext(
            customerSegment = CustomerSegment.REGULAR,
            cartItems = listOf(cartItem)
        )

        // When
        val result = promotionEngine.applyPromotions(context, listOf(promotion))

        // Then
        assertEquals(BigDecimal("20.00"), result.cartItems[0].discount)
        assertEquals(1, result.appliedPromotions.size)
        assertEquals("10% off ELECTRONICS category", result.appliedPromotions[0].description)
        assertEquals(BigDecimal("20.00"), result.appliedPromotions[0].discountAmount)
    }

    @Test
    fun `should apply buy x get y promotion`() {
        // Given
        val productId = UUID.randomUUID()
        val product = Product(
            id = productId,
            name = "Special Item",
            category = ProductCategory.GENERAL,
            price = BigDecimal("50.00"),
            stock = 10
        )

        val cartItem = CartLineItem(
            product = product,
            quantity = 5, // Buy 3 get 1 free, so 1 free item
            subtotal = BigDecimal("250.00")
        )

        val promotion = Promotion(
            id = UUID.randomUUID(),
            type = PromotionType.BUY_X_GET_Y,
            targetProductId = productId,
            buyQuantity = 3,
            getQuantity = 1,
            priority = 1
        )

        val context = PromotionContext(
            customerSegment = CustomerSegment.REGULAR,
            cartItems = listOf(cartItem)
        )

        // When
        val result = promotionEngine.applyPromotions(context, listOf(promotion))

        // Then
        assertEquals(BigDecimal("50.00"), result.cartItems[0].discount) // 1 free item
        assertEquals(1, result.appliedPromotions.size)
        assertTrue(result.appliedPromotions[0].description.contains("1 free items"))
    }

    @Test
    fun `should apply promotions in priority order`() {
        // Given
        val product = Product(
            id = UUID.randomUUID(),
            name = "Electronics Item",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("100.00"),
            stock = 10
        )

        val cartItem = CartLineItem(
            product = product,
            quantity = 1,
            subtotal = BigDecimal("100.00")
        )

        val lowPriorityPromotion = Promotion(
            id = UUID.randomUUID(),
            type = PromotionType.PERCENT_OFF_CATEGORY,
            targetCategory = ProductCategory.ELECTRONICS,
            discountPercentage = BigDecimal("0.2"), // 20%
            priority = 10
        )

        val highPriorityPromotion = Promotion(
            id = UUID.randomUUID(),
            type = PromotionType.PERCENT_OFF_CATEGORY,
            targetCategory = ProductCategory.ELECTRONICS,
            discountPercentage = BigDecimal("0.1"), // 10%
            priority = 1
        )

        val context = PromotionContext(
            customerSegment = CustomerSegment.REGULAR,
            cartItems = listOf(cartItem)
        )

        // When
        val result = promotionEngine.applyPromotions(
            context,
            listOf(lowPriorityPromotion, highPriorityPromotion)
        )

        // Then
        // Both promotions should apply, but high priority first
        assertEquals(BigDecimal("30.00"), result.cartItems[0].discount) // 10% + 20% of remaining
        assertEquals(2, result.appliedPromotions.size)
    }
}