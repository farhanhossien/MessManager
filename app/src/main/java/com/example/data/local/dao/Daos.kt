package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.DepositEntity
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.MonthlyClosingEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?
}

@Dao
interface MessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMess(mess: MessEntity)

    @Update
    suspend fun updateMess(mess: MessEntity)

    @Query("SELECT * FROM messes WHERE id = :id LIMIT 1")
    suspend fun getMessById(id: String): MessEntity?

    @Query("SELECT * FROM messes WHERE id = :id LIMIT 1")
    fun observeMessById(id: String): Flow<MessEntity?>

    @Query("SELECT * FROM messes WHERE inviteCode = :code LIMIT 1")
    suspend fun getMessByInviteCode(code: String): MessEntity?

    @Query("SELECT m.* FROM messes m INNER JOIN members mem ON m.id = mem.messId WHERE mem.userId = :userId AND mem.status IN ('ACTIVE', 'PENDING')")
    fun observeUserMesses(userId: String): Flow<List<MessEntity>>

    @Query("SELECT * FROM messes")
    suspend fun getAllMesses(): List<MessEntity>
}

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("SELECT * FROM members WHERE messId = :messId AND status = 'ACTIVE' ORDER BY name ASC")
    fun observeActiveMembers(messId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE messId = :messId AND status = 'PENDING' ORDER BY joinDate DESC")
    fun observePendingMembers(messId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE messId = :messId")
    suspend fun getAllMembers(messId: String): List<MemberEntity>

    @Query("SELECT * FROM members WHERE messId = :messId ORDER BY name ASC")
    fun observeAllMembers(messId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE messId = :messId AND status = 'ACTIVE'")
    suspend fun getActiveMembersSync(messId: String): List<MemberEntity>

    @Query("SELECT * FROM members WHERE messId = :messId AND status IN ('ACTIVE', 'REMOVED') ORDER BY name ASC")
    fun observeMembersForCalculation(messId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE messId = :messId AND status IN ('ACTIVE', 'REMOVED')")
    suspend fun getMembersForCalculationSync(messId: String): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE messId = :messId AND userId = :userId LIMIT 1")
    suspend fun getMemberByMessAndUser(messId: String, userId: String): MemberEntity?

    @Query("SELECT * FROM members WHERE messId = :messId AND userId = :userId LIMIT 1")
    fun observeMemberByMessAndUser(messId: String, userId: String): Flow<MemberEntity?>

    @Query("SELECT COUNT(*) FROM members WHERE messId = :messId AND status = 'PENDING'")
    fun observePendingCount(messId: String): Flow<Int>
}

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMeal(meal: MealEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntryEntity>)

    @Query("SELECT * FROM meals WHERE messId = :messId AND date = :date")
    fun observeMealsByDate(messId: String, date: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meals WHERE messId = :messId AND date LIKE :monthPrefix || '%'")
    fun observeMealsForMonth(messId: String, monthPrefix: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meals WHERE messId = :messId AND date LIKE :monthPrefix || '%'")
    suspend fun getMealsForMonthSync(messId: String, monthPrefix: String): List<MealEntryEntity>

    @Query("SELECT * FROM meals WHERE messId = :messId AND memberId = :memberId AND date = :date LIMIT 1")
    suspend fun getMealEntry(messId: String, memberId: String, date: String): MealEntryEntity?
}

@Dao
interface BazarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: BazarExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: BazarExpenseEntity)

    @Query("SELECT * FROM bazar_expenses WHERE messId = :messId AND date LIKE :monthPrefix || '%' ORDER BY date DESC, createdAt DESC")
    fun observeExpensesForMonth(messId: String, monthPrefix: String): Flow<List<BazarExpenseEntity>>

    @Query("SELECT * FROM bazar_expenses WHERE messId = :messId AND date LIKE :monthPrefix || '%'")
    suspend fun getExpensesForMonthSync(messId: String, monthPrefix: String): List<BazarExpenseEntity>

    @Query("SELECT * FROM bazar_expenses WHERE messId = :messId ORDER BY date DESC")
    fun observeAllExpenses(messId: String): Flow<List<BazarExpenseEntity>>
}

@Dao
interface DepositDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity)

    @Delete
    suspend fun deleteDeposit(deposit: DepositEntity)

    @Query("SELECT * FROM deposits WHERE messId = :messId AND date LIKE :monthPrefix || '%' ORDER BY date DESC, createdAt DESC")
    fun observeDepositsForMonth(messId: String, monthPrefix: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE messId = :messId AND date LIKE :monthPrefix || '%'")
    suspend fun getDepositsForMonthSync(messId: String, monthPrefix: String): List<DepositEntity>

    @Query("SELECT * FROM deposits WHERE messId = :messId ORDER BY date DESC")
    fun observeAllDeposits(messId: String): Flow<List<DepositEntity>>
}

@Dao
interface FixedCostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedCost(fixedCost: FixedCostEntity)

    @Delete
    suspend fun deleteFixedCost(fixedCost: FixedCostEntity)

    @Query("SELECT * FROM fixed_costs WHERE messId = :messId AND month = :month ORDER BY createdAt ASC")
    fun observeFixedCostsForMonth(messId: String, month: String): Flow<List<FixedCostEntity>>

    @Query("SELECT * FROM fixed_costs WHERE messId = :messId AND month = :month")
    suspend fun getFixedCostsForMonthSync(messId: String, month: String): List<FixedCostEntity>
}

@Dao
interface MonthlyClosingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosing(closing: MonthlyClosingEntity)

    @Query("SELECT * FROM monthly_closings WHERE messId = :messId AND month = :month LIMIT 1")
    suspend fun getClosing(messId: String, month: String): MonthlyClosingEntity?

    @Query("SELECT * FROM monthly_closings WHERE messId = :messId AND month = :month LIMIT 1")
    fun observeClosing(messId: String, month: String): Flow<MonthlyClosingEntity?>

    @Query("SELECT * FROM monthly_closings WHERE messId = :messId ORDER BY month DESC")
    fun observeClosings(messId: String): Flow<List<MonthlyClosingEntity>>

    @Query("DELETE FROM monthly_closings WHERE messId = :messId AND month = :month")
    suspend fun deleteClosing(messId: String, month: String)
}
