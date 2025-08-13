package com.example.promoquoter.controller

import com.example.promoquoter.dto.CreatePromotionRequest
import com.example.promoquoter.dto.PromotionResponse
import com.example.promoquoter.service.PromotionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/promotions")
class PromotionController(private val promotionService: PromotionService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPromotions(@Valid @RequestBody requests: List<CreatePromotionRequest>): List<PromotionResponse> {
        return promotionService.createPromotions(requests)
    }
}