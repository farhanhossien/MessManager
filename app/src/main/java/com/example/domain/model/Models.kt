package com.example.domain.model

data class MemberCalculation(
    val memberId: String,
    val name: String,
    val phone: String?,
    val role: String,
    val type: String,
    val totalBreakfast: Float = 0f,
    val totalLunch: Float = 0f,
    val totalDinner: Float = 0f,
    val totalGuestMeals: Float = 0f,
    val totalMeals: Float = 0f,
    val mealCost: Double = 0.0,
    val fixedCostShare: Double = 0.0,
    val openingBalance: Double = 0.0,
    val totalDeposits: Double = 0.0,
    val totalCost: Double = 0.0,
    val netBalance: Double = 0.0 // positive: gets back (পাবে), negative: owes (দিবে)
)

data class MessCalculationSummary(
    val month: String = "",
    val totalBazarCost: Double = 0.0,
    val totalMeals: Float = 0f,
    val mealRate: Double = 0.0,
    val totalFixedCosts: Double = 0.0,
    val totalDeposits: Double = 0.0,
    val todayBreakfastCount: Float = 0f,
    val todayLunchCount: Float = 0f,
    val todayDinnerCount: Float = 0f,
    val todayTotalMeals: Float = 0f,
    val memberCalculations: List<MemberCalculation> = emptyList(),
    val isClosed: Boolean = false
)

enum class AppLanguage {
    BANGLA, ENGLISH
}
