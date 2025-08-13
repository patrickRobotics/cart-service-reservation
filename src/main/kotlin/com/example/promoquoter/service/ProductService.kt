package com.example.promoquoter.service

import com.example.promoquoter.domain.Product
import com.example.promoquoter.dto.CreateProductRequest
import com.example.promoquoter.dto.ProductResponse
import com.example.promoquoter.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class ProductService(private val productRepository: ProductRepository) {

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

        return savedProducts.map { product ->
            ProductResponse(
                id = product.id!!,
                name = product.name,
                category = product.category,
                price = product.price,
                stock = product.stock
            )
        }
    }
}
