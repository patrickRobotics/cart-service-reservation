package com.example.promoquoter.service

import com.example.promoquoter.domain.*
import com.example.promoquoter.dto.CartItemRequest
import com.example.promoquoter.dto.CartQuoteRequest
import com.example.promoquoter.promotion.CartLineItem
import com.example.promoquoter.promotion.PromotionContext
import com.example.promoquoter.promotion.PromotionEngine
import com.example.promoquoter.repository.OrderRepository
import com.example.promoquoter.repository.ProductRepository
import com.example.promoquoter.repository.PromotionRepository
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals


@SpringBootTest
class CartServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val promotionRepository = mockk<PromotionRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val promotionEngine = mockk<PromotionEngine>()

    private val cartService = CartService(
        productRepository, promotionRepository, orderRepository, promotionEngine
    )

    @Test
    fun `should generate quote successfully`() {
        // Given
        val productId = UUID.randomUUID()
        val product = Product(
            id = productId,
            name = "Test Product",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("99.99"),
            stock = 10
        )

        val request = CartQuoteRequest(
            items = listOf(CartItemRequest(productId, 2)),
            customerSegment = CustomerSegment.REGULAR
        )

        // Create actual CartLineItem instead of mocking
        val cartLineItem = CartLineItem(
            product = product,
            quantity = 2,
            subtotal = BigDecimal("199.98"),
            discount = BigDecimal.ZERO
        )

        val promotionContext = PromotionContext(
            customerSegment = CustomerSegment.REGULAR,
            cartItems = listOf(cartLineItem),
            appliedPromotions = mutableListOf()
        )

        every { productRepository.findAllByIdIn(listOf(productId)) } returns listOf(product)
        every { promotionRepository.findByActiveOrderByPriority(true) } returns emptyList()
        every { promotionEngine.applyPromotions(any(), any()) } returns promotionContext

        // When
        val result = cartService.generateQuote(request)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(BigDecimal("199.98"), result.subtotal)
        assertEquals(BigDecimal.ZERO, result.totalDiscount)
        assertEquals(BigDecimal("199.98"), result.finalTotal)

        verify { productRepository.findAllByIdIn(listOf(productId)) }
        verify { promotionRepository.findByActiveOrderByPriority(true) }
    }

    @Test
    fun `should throw exception when product not found`() {
        // Given
        val productId = UUID.randomUUID()
        val request = CartQuoteRequest(
            items = listOf(CartItemRequest(productId, 2)),
            customerSegment = CustomerSegment.REGULAR
        )

        every { productRepository.findAllByIdIn(listOf(productId)) } returns emptyList()

        // When & Then
        assertThrows<ProductNotFoundException> {
            cartService.generateQuote(request)
        }
    }

    @Test
    fun `should throw exception when insufficient stock`() {
        // Given
        val productId = UUID.randomUUID()
        val product = Product(
            id = productId,
            name = "Test Product",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("99.99"),
            stock = 1 // Less than requested quantity
        )

        val request = CartQuoteRequest(
            items = listOf(CartItemRequest(productId, 2)),
            customerSegment = CustomerSegment.REGULAR
        )

        every { productRepository.findAllByIdIn(listOf(productId)) } returns listOf(product)

        // When & Then
        assertThrows<InsufficientStockException> {
            cartService.generateQuote(request)
        }
    }
}