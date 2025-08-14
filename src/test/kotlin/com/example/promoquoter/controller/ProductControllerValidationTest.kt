package com.example.promoquoter.controller

import com.example.promoquoter.domain.Product
import com.example.promoquoter.domain.ProductCategory
import com.example.promoquoter.dto.CreateProductRequest
import com.example.promoquoter.repository.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertFalse

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["spring.datasource.url=jdbc:h2:mem:testdb"])
@Transactional
class ProductControllerValidationTest(
    @Autowired val productRepository: ProductRepository
) {
    lateinit var testProduct: Product

    @BeforeEach
    fun setup() {
        testProduct = productRepository.save(
            Product(
                name = "Test Product",
                category = ProductCategory.ELECTRONICS,
                price = BigDecimal("49.99"),
                stock = 1
            )
        )
    }

    // Set 1: POST endpoints
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
            post("/products")
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
    fun `should handle creation a single product`() {
        val productData = """{
            "name": "Test Product",
            "category": "EECTRONICS",
            "price": 99.99,
            "stock": 10
        }"""

        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productData)
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
    fun `should handle creation of batch products list`() {
        val batchRequest = """[
            {
                "name": "Valid Product",
                "category": "ELECTRONICS",
                "price": 99.99,
                "stock": 10
            },
            {
                "name": "Dera dress",
                "category": "CLOTHING", 
                "price": 400.00,
                "stock": 2
            }
        ]"""
        mockMvc.perform(
            post("/products/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchRequest)
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should handle mixed valid and invalid batch products creation`() {
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
            post("/products/batch")
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
        val invalidRequest = CreateProductRequest(
            name = "",
            category = ProductCategory.ELECTRONICS,
            price = BigDecimal("0.00"), // Below minimum
            stock = -1  // Stock below 0
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
            .andExpect(jsonPath("$.message").value("Invalid input parameters"))
            .andExpect(jsonPath("$.details").exists())
            .andExpect(jsonPath("$.details.fieldErrors").exists())
    }

    // Set 2: GET endpoint

    @Test
    fun `should return a list of all Products`() {
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Test Product"))
            .andExpect(jsonPath("$[0].category").value("ELECTRONICS"))
            .andExpect(jsonPath("$[0].price").value(49.99))
    }

    @Test
    fun `should return product when GET by id`() {
        mockMvc.perform(get("/products/${testProduct.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Test Product"))
    }

    @Test
    fun `should return Product Not Found 404 error when GET by id for missing ID`() {
        mockMvc.perform(get("/products/37acba80-c90e-4bc6-8adb-0dfa38a47f37"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Product Not Found"))
            .andExpect(jsonPath("$.message").value("Product with id 37acba80-c90e-4bc6-8adb-0dfa38a47f37 not found"))
    }

    @Test
    fun `should return Error 400 when GET by id for invalid parameter type for ID`() {
        mockMvc.perform(get("/products/missing-product-id"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id': expected UUID"))
            .andExpect(jsonPath("$.error").value("Parameter Type Mismatch"))
    }

    // Set 3: PUT endpoint

    @Test
    fun `should update a product fields given its ID`() {
        val updateFields = """{
            "name": "New Product",
            "stock": 10
        }"""
        mockMvc.perform(
            put("/products/${testProduct.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateFields)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.stock").value(10))

        // then - verify by retrieving it
        mockMvc.perform(get("/products/${testProduct.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.stock").value(10))
            .andExpect(jsonPath("$.category").value("ELECTRONICS"))
            .andExpect(jsonPath("$.price").value(BigDecimal("49.99")))
    }

    // Set 4. Delete endpoint

    @Test
    fun `should delete a product given its ID`() {
        mockMvc.perform(
            delete("/products/${testProduct.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        // Assert — verify product is gone
        assertFalse(productRepository.existsById(testProduct.id!!))
    }

    @Test
    fun `should return 404 not found error when a product ID can't be found`() {
        mockMvc.perform(
            delete("/products/37acba80-c90e-4bc6-8adb-0dfa38a47f37")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Product Not Found"))
            .andExpect(jsonPath("$.message").value("Product with id 37acba80-c90e-4bc6-8adb-0dfa38a47f37 not found"))
    }
}