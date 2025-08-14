package com.example.promoquoter.controller

import com.example.promoquoter.dto.CreateProductRequest
import com.example.promoquoter.dto.ProductResponse
import com.example.promoquoter.dto.UpdateProductRequest
import com.example.promoquoter.service.ProductService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID


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

    @PostMapping("/single")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ProductResponse {
        logger.info("Processing request to create product ${request.name}")
        return productService.createProduct(request)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: UUID): ProductResponse {
        logger.info("Processing request to fetch product by id: ${id}")
        return productService.getProductById(id)
    }

    @GetMapping
    fun getAllProducts(): List<ProductResponse> {
        logger.info("Processing request to fetch all products")
        return productService.getAllProducts()
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateProductRequest
    ): ProductResponse {
        logger.info("Processing request to update product by id: ${id}")
        return productService.updateProduct(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@PathVariable id: UUID) {
        logger.info("Processing request to delete product by id: ${id}")
        productService.deleteProduct(id)
    }
}