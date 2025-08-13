package com.example.promoquoter.promotion


interface PromotionRule {
    fun canApply(context: PromotionContext): Boolean
    fun apply(context: PromotionContext): PromotionContext
    val priority: Int
    val description: String
}