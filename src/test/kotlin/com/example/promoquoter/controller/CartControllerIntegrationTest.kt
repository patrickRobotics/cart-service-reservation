package com.example.promoquoter.controller

import com.example.promoquoter.domain.CustomerSegment
import com.example.promoquoter.domain.ProductCategory
import com.example.promoquoter.dto.*
import com.example.promoquoter.dto.CartItemRequest
import com.example.promoquoter.dto.CartQuoteRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.datasource.url=jdbc:h2:mem:testdb"])
@Transactional
class CartControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create product and generate quote`() {
        // Create product first
        val productRequest = CreateProductRequest(
            name = "Test Product",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("99.99"),
            stock = 10
        )

        val productResult = mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest))
        ).andExpect(status().isCreated)
            .andReturn()

        val productResponse = objectMapper.readValue(
            productResult.response.contentAsString,
            ProductResponse::class.java
        )

        // Generate quote
        val quoteRequest = CartQuoteRequest(
            items = listOf(CartItemRequest(productResponse.id, 2)),
            customerSegment = CustomerSegment.REGULAR
        )

        mockMvc.perform(
            post("/cart/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quoteRequest))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.finalTotal").value(199.98))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
    }

    @Test
    fun `should handle invalid cart request`() {
        val invalidRequest = CartQuoteRequest(
            items = emptyList(), // Empty items should fail validation
            customerSegment = CustomerSegment.REGULAR
        )

        mockMvc.perform(
            post("/cart/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        ).andExpect(status().isBadRequest)
    }
}