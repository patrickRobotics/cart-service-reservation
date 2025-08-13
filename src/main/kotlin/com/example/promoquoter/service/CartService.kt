package com.example.promoquoter.service

import com.example.promoquoter.controller.CartController
import com.example.promoquoter.domain.*
import com.example.promoquoter.dto.*
import com.example.promoquoter.promotion.*
import com.example.promoquoter.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.math.log


@Service
@Transactional
class CartService(
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val orderRepository: OrderRepository,
    private val promotionEngine: PromotionEngine
) {

    private val logger = LoggerFactory.getLogger(CartController::class.java)

    fun generateQuote(request: CartQuoteRequest): CartQuoteResponse {
        val products = getProductsForCart(request.items)
        logger.info("Order products: ${products}")
        validateCartItems(request.items, products)

        val context = createPromotionContext(request, products)
        val activePromotions = promotionRepository.findByActiveOrderByPriority(true)
        val finalContext = promotionEngine.applyPromotions(context, activePromotions)

        return buildQuoteResponse(finalContext)
    }

    fun confirmCart(request: CartQuoteRequest, idempotencyKey: String?): CartConfirmResponse {
        logger.info("Confirming order")

        // Check for existing order with same idempotency key
        if (idempotencyKey != null) {
            val existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey)
            if (existingOrder.isPresent) {
                return convertOrderToConfirmResponse(existingOrder.get())
            }
        }

        // Get products with pessimistic locks for inventory reservation
        val productIds = request.items.map { it.productId }
        val lockedProducts = productIds.mapNotNull { id ->
            productRepository.findByIdWithLock(id).orElse(null)
        }

        if (lockedProducts.size != productIds.size) {
            val missingIds = productIds - lockedProducts.map { it.id }.toSet()
            throw ProductNotFoundException("Products not found: $missingIds")
        }

        validateStockAvailability(request.items, lockedProducts)

        // Generate quote with current inventory
        val context = createPromotionContext(request, lockedProducts)
        val activePromotions = promotionRepository.findByActiveOrderByPriority(true)
        val finalContext = promotionEngine.applyPromotions(context, activePromotions)

        // Reserve inventory
        val updatedProducts = reserveInventory(request.items, lockedProducts)
        productRepository.saveAll(updatedProducts)

        // Create order
        val savedOrder = createOrder(request, finalContext, idempotencyKey)

        return convertOrderToConfirmResponse(savedOrder)
    }

    private fun getProductsForCart(items: List<CartItemRequest>): List<Product> {
        val productIds = items.map { it.productId }
        val products = productRepository.findAllByIdIn(productIds)

        if (products.size != productIds.size) {
            val foundIds = products.map { it.id }.toSet()
            val missingIds = productIds - foundIds
            throw ProductNotFoundException("Products not found: $missingIds")
        }

        return products
    }

    private fun validateCartItems(items: List<CartItemRequest>, products: List<Product>) {
        val productMap = products.associateBy { it.id }

        items.forEach { item ->
            val product = productMap[item.productId]!!
            if (product.stock < item.qty) {
                logger.info("Product ${product.name} stock is insufficient to fulfill ${item.qty} items")
                throw InsufficientStockException(
                    "Insufficient stock for product ${product.name}. Available: ${product.stock}, Requested: ${item.qty}"
                )
            }
        }
    }

    private fun validateStockAvailability(items: List<CartItemRequest>, products: List<Product>) {
        val productMap = products.associateBy { it.id }

        items.forEach { item ->
            val product = productMap[item.productId]!!
            if (product.stock < item.qty) {
                throw InsufficientStockException(
                    "Insufficient stock for product ${product.name}. Available: ${product.stock}, Requested: ${item.qty}"
                )
            }
        }
    }

    private fun createPromotionContext(request: CartQuoteRequest, products: List<Product>): PromotionContext {
        logger.info("Checking promotion discounts for the ordered products")

        val productMap = products.associateBy { it.id }
        val cartItems = request.items.map { item ->
            val product = productMap[item.productId]!!
            val subtotal = product.price.multiply(BigDecimal(item.qty))
            CartLineItem(product, item.qty, subtotal)
        }

        return PromotionContext(request.customerSegment, cartItems)
    }

    private fun buildQuoteResponse(context: PromotionContext): CartQuoteResponse {
        val lineItems = context.cartItems.map { item ->
            LineItemResponse(
                productId = item.product.id!!,
                productName = item.product.name,
                quantity = item.quantity,
                unitPrice = item.product.price,
                lineSubtotal = item.subtotal,
                lineDiscount = item.discount,
                lineFinal = item.finalPrice
            )
        }

        val appliedPromotions = context.appliedPromotions.map { promo ->
            AppliedPromotionResponse(
                promotionId = promo.promotionId,
                type = promo.type,
                description = promo.description,
                discountAmount = promo.discountAmount
            )
        }

        val subtotal = context.cartItems.sumOf { it.subtotal }
        val totalDiscount = context.cartItems.sumOf { it.discount }
        val finalTotal = subtotal - totalDiscount

        return CartQuoteResponse(lineItems, appliedPromotions, subtotal, totalDiscount, finalTotal)
    }

    private fun reserveInventory(items: List<CartItemRequest>, products: List<Product>): List<Product> {
        val productMap = products.associateBy { it.id }

        return items.map { item ->
            val product = productMap[item.productId]!!
            product.copy(stock = product.stock - item.qty)
        }
    }

    private fun createOrder(
        request: CartQuoteRequest, context: PromotionContext, idempotencyKey: String?
    ): Order {
        val totalAmount = context.cartItems.sumOf { it.finalPrice }

        // Create order
        val order = Order(
            idempotencyKey = idempotencyKey,
            totalAmount = totalAmount,
            customerSegment = request.customerSegment
        )

        // Create order items and set the order reference
        context.cartItems.forEach { item ->
            val orderItem = OrderItem(
                order = order,
                productId = item.product.id!!,
                quantity = item.quantity,
                unitPrice = item.product.price,
                totalPrice = item.finalPrice
            )
            order.addItem(orderItem)
        }

        // Save the order with cascade to save items automatically
        return orderRepository.save(order)
    }

    private fun convertOrderToConfirmResponse(order: Order): CartConfirmResponse {
        val lineItems = order.items.map { item ->
            LineItemResponse(
                productId = item.productId,
                productName = "", // Todo: Fetch real product name
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                lineSubtotal = item.unitPrice.multiply(BigDecimal(item.quantity)),
                lineDiscount = item.unitPrice.multiply(BigDecimal(item.quantity)) - item.totalPrice,
                lineFinal = item.totalPrice
            )
        }

        val subtotal = lineItems.sumOf { it.lineSubtotal }
        val totalDiscount = lineItems.sumOf { it.lineDiscount }

        return CartConfirmResponse(
            orderId = order.id!!,
            items = lineItems,
            appliedPromotions = emptyList(), // Todo: store and retrieve applied promotions
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            finalTotal = order.totalAmount
        )
    }
}

class ProductNotFoundException(message: String) : RuntimeException(message)
class InsufficientStockException(message: String) : RuntimeException(message)
