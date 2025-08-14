package com.example.promoquoter.controller

import com.example.promoquoter.domain.ProductCategory
import com.example.promoquoter.dto.CreateProductRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
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
class ProductControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should handle invalid JSON syntax`() {
        val invalidJson = """{"name": "Test", "category": "ELECTRONICS", "price": 99.99, "stock": }""" // Missing value for stock

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should handle malformed JSON`() {
        val malformedJson = """{"name": "Test" "category": "ELECTRONICS"}""" // Missing comma

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
    }

    @Test
    fun `should handle validation errors with detailed field messages`() {
        val invalidRequest = CreateProductRequest(
            name = "", // Blank name
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("-1.00"), // Negative price
            stock = -5 // Negative stock
        )

        mockMvc.perform(
            post("/products/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.details.fieldErrors").exists())
            .andExpect(jsonPath("$.details.fieldErrors.name").value("Product name cannot be blank"))
            .andExpect(jsonPath("$.details.fieldErrors.price").value("Price must be at least 0.01"))
            .andExpect(jsonPath("$.details.fieldErrors.stock").value("Stock cannot be negative"))
    }

    @Test
    fun `should handle invalid enum values`() {
        val invalidEnumJson = """[{
            "name": "Test Product",
            "category": "INVALID_CATEGORY",
            "price": 99.99,
            "stock": 10
        }]"""

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEnumJson)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
            .andExpect(jsonPath("$.message").value(containsString("Invalid value for field")))
    }

    @Test
    fun `should handle missing required fields`() {
        val missingFieldsJson = """[{
            "name": "Test Product"
        }]""" // Missing category, price, stock

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingFieldsJson)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
    }

    @Test
    fun `should handle wrong data types`() {
        val wrongTypesJson = """[{
            "name": "Test Product",
            "category": "ELECTRONICS",
            "price": "not-a-number",
            "stock": "not-an-integer"
        }]"""

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongTypesJson)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
    }

    @Test
    fun `should handle empty request body`() {
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid Request Format"))
    }

    @Test
    fun `should handle null request body`() {
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `should handle empty list`() {
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]")
        ).andExpect(status().isCreated) // Empty list is actually valid, returns empty response
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `should handle mixed valid and invalid products in list`() {
        val mixedRequest = """[
            {
                "name": "Valid Product",
                "category": "ELECTRONICS",
                "price": 99.99,
                "stock": 10
            },
            {
                "name": "",
                "category": "ELECTRONICS",
                "price": -1.00,
                "stock": -5
            }
        ]"""

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mixedRequest)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.details.fieldErrors").exists())
            .andExpect(jsonPath("$.message").value("Invalid method parameters"))
            .andExpect(jsonPath("$.details.fieldErrors.name").value("Product name cannot be blank"))
            .andExpect(jsonPath("$.details.fieldErrors.price").value("Price must be at least 0.01"))
            .andExpect(jsonPath("$.details.fieldErrors.stock").value("Stock cannot be negative"))
    }

    @Test
    fun `should provide helpful error structure`() {
        val invalidRequest = listOf(
            CreateProductRequest(
                name = "",
                category = ProductCategory.ELECTRONICS,
                price = BigDecimal("0.00"), // Below minimum
                stock = -1
            )
        )

        val result = mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        ).andExpect(status().isBadRequest)
            .andReturn()

        val response = result.response.contentAsString
        println("Error Response Structure:")
        println(response)

        // Verify error response structure
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.message").value("Invalid method parameters"))
            .andExpect(jsonPath("$.details").exists())
            .andExpect(jsonPath("$.details.fieldErrors").exists())
    }
}