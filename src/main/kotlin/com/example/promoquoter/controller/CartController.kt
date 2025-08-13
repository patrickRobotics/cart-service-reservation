package com.example.promoquoter.controller

import com.example.promoquoter.dto.CartConfirmResponse
import com.example.promoquoter.dto.CartQuoteRequest
import com.example.promoquoter.dto.CartQuoteResponse
import com.example.promoquoter.service.CartService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/cart")
class CartController(private val cartService: CartService) {
    private val logger = LoggerFactory.getLogger(CartController::class.java)

    @PostMapping("/quote")
    fun generateQuote(@Valid @RequestBody request: CartQuoteRequest): CartQuoteResponse {
        logger.info("Received request to place order ${request}")
        return cartService.generateQuote(request)
    }

    @PostMapping("/confirm")
    fun confirmCart(
        @Valid @RequestBody request: CartQuoteRequest,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?
    ): CartConfirmResponse {
        logger.info("Received request to confirm order ${request}")
        return cartService.confirmCart(request, idempotencyKey)
    }
}