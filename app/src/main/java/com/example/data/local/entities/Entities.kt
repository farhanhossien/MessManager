package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "", // Firebase UID
    val phone: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "messes")
data class MessEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val adminId: String = "", // Firebase UID of mess creator
    val cutoffHour: Int = 22, // 10:00 PM cutoff for next day meal toggle
    val currentMonth: String = "", // e.g. "2026-10"
    val isClosed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String = "", // Firebase UID for ACCOUNT members; UUID for MANAGED
    val messId: String = "",
    val userId: String? = null, // Firebase UID for ACCOUNT members; null for MANAGED
    val name: String = "",
    val phone: String? = null,
    val role: String = "MEMBER", // "ADMIN", "MANAGER", "MEMBER"
    val type: String = "ACCOUNT", // "ACCOUNT" or "MANAGED"
    val status: String = "ACTIVE", // "ACTIVE", "PENDING", "REJECTED", "REMOVED"
    val joinDate: String = "", // "YYYY-MM-DD"
    val leaveDate: String? = null,
    val openingBalance: Double = 0.0, // Carry forward balance from previous month
    val createdBy: String? = null // Admin UID who created the managed member
)

@IgnoreExtraProperties
@Entity(tableName = "meals")
data class MealEntryEntity(
    @PrimaryKey val id: String = "",
    val messId: String = "",
    val memberId: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val breakfast: Float = 0f,
    val lunch: Float = 1f,
    val dinner: Float = 1f,
    val guestMeals: Float = 0f,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "bazar_expenses")
data class BazarExpenseEntity(
    @PrimaryKey val id: String = "",
    val messId: String = "",
    val buyerMemberId: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val amount: Double = 0.0,
    val category: String = "others", // "rice", "dal", "fish", "meat", "vegetables", "spices", "oil", "others"
    val note: String = "",
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey val id: String = "",
    val messId: String = "",
    val memberId: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val amount: Double = 0.0,
    val paymentMethod: String = "CASH", // "BKASH", "NAGAD", "ROCKET", "CASH", "BANK"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "fixed_costs")
data class FixedCostEntity(
    @PrimaryKey val id: String = "",
    val messId: String = "",
    val title: String = "",
    val category: String = "others", // "maid", "gas", "electricity", "wifi", "rent", "water", "others"
    val amount: Double = 0.0,
    val month: String = "", // "YYYY-MM"
    val splitType: String = "EQUAL", // "EQUAL" or "CUSTOM"
    val customSharesJson: String = "{}", // JSON map: { memberId: amount }
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "monthly_closings")
data class MonthlyClosingEntity(
    @PrimaryKey val id: String = "",
    val messId: String = "",
    val month: String = "", // "YYYY-MM"
    val totalBazar: Double = 0.0,
    val totalMeals: Float = 0f,
    val mealRate: Double = 0.0,
    val totalFixedCosts: Double = 0.0,
    val previousBalancesJson: String = "", // JSON map of memberId to previous openingBalance
    val closedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
