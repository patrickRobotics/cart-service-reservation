package com.example.promoquoter.controller

import com.example.promoquoter.dto.CreateProductRequest
import com.example.promoquoter.dto.ProductResponse
import com.example.promoquoter.service.ProductService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProducts(@Valid @RequestBody requests: List<CreateProductRequest>): List<ProductResponse> {
        logger.info("Received request to create ${requests.size} products")
        val createdProducts = productService.createProducts(requests)
        logger.info("Created ${createdProducts.size} products successfully")
        return createdProducts
    }
}