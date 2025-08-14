package com.example.promoquoter.service

import com.example.promoquoter.domain.Product
import com.example.promoquoter.dto.CreateProductRequest
import com.example.promoquoter.dto.ProductResponse
import com.example.promoquoter.dto.UpdateProductRequest
import com.example.promoquoter.repository.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


@Service
@Transactional
class ProductService(private val productRepository: ProductRepository) {

    fun createProduct(request: CreateProductRequest): ProductResponse {
        val product = Product(
            name = request.name,
            category = request.category,
            price = request.price,
            stock = request.stock
        )

        val savedProduct = productRepository.save(product)
        return mapToResponse(savedProduct)
    }

    fun createProducts(requests: List<CreateProductRequest>): List<ProductResponse> {
        val products = requests.map { request ->
            Product(
                name = request.name,
                category = request.category,
                price = request.price,
                stock = request.stock
            )
        }

        val savedProducts = productRepository.saveAll(products)
        return savedProducts.map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: UUID): ProductResponse {
        val product = productRepository.findByIdOrNull(id)
            ?: throw ProductNotFoundException("Product with id $id not found")

        return mapToResponse(product)
    }

    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductResponse> {
        val products = productRepository.findAll()
        return products.map { mapToResponse(it) }
    }

    fun updateProduct(id: UUID, request: UpdateProductRequest): ProductResponse {
        val existingProduct = productRepository.findByIdOrNull(id)
            ?: throw ProductNotFoundException("Product with id $id not found")

        val updatedProduct = existingProduct.copy(
            name = request.name ?: existingProduct.name,
            category = request.category ?: existingProduct.category,
            price = request.price ?: existingProduct.price,
            stock = request.stock ?: existingProduct.stock
        )

        val savedProduct = productRepository.save(updatedProduct)
        return mapToResponse(savedProduct)
    }

    fun deleteProduct(id: UUID) {
        val product = productRepository.findByIdOrNull(id)
            ?: throw ProductNotFoundException("Product with id $id not found")

        productRepository.delete(product)
    }

    private fun mapToResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id!!,
            name = product.name,
            category = product.category,
            price = product.price,
            stock = product.stock
        )
    }
}
