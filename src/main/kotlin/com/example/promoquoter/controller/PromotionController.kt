package com.example.promoquoter.controller

import com.example.promoquoter.dto.CreatePromotionRequest
import com.example.promoquoter.dto.PromotionResponse
import com.example.promoquoter.service.PromotionService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/promotions")
class PromotionController(private val promotionService: PromotionService) {
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPromotions(@Valid @RequestBody requests: List<CreatePromotionRequest>): List<PromotionResponse> {
        return promotionService.createPromotions(requests)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPromotio(@Valid @RequestBody request: CreatePromotionRequest): PromotionResponse {
        logger.info("Processing request to create a Promotion ${request.type}")
        return promotionService.createPromotion(request)
    }
}