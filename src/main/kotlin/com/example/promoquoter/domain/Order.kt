package com.example.promoquoter.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "idempotency_key", unique = true)
    val idempotencyKey: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val customerSegment: CustomerSegment,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf()
) {
    constructor() : this(
        null,
        null,
        BigDecimal.ZERO,
        CustomerSegment.REGULAR,
        LocalDateTime.now(),
        mutableListOf()
    )

    fun addItem(item: OrderItem) {
        items.add(item)
    }
}


@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order? = null,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false, precision = 10, scale = 2)
    val totalPrice: BigDecimal
) {
    constructor() : this(
        null,
        null,
        UUID.randomUUID(),
        0,
        BigDecimal.ZERO,
        BigDecimal.ZERO
    )
}


enum class CustomerSegment {
    REGULAR, PREMIUM, VIP
}