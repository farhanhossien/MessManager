package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BazarDao
import com.example.data.local.dao.DepositDao
import com.example.data.local.dao.FixedCostDao
import com.example.data.local.dao.MealDao
import com.example.data.local.dao.MemberDao
import com.example.data.local.dao.MessDao
import com.example.data.local.dao.MonthlyClosingDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.DepositEntity
import com.example.data.local.entities.FixedCostEntity
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import com.example.data.local.entities.MessEntity
import com.example.data.local.entities.MonthlyClosingEntity
import com.example.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MessEntity::class,
        MemberEntity::class,
        MealEntryEntity::class,
        BazarExpenseEntity::class,
        DepositEntity::class,
        FixedCostEntity::class,
        MonthlyClosingEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messDao(): MessDao
    abstract fun memberDao(): MemberDao
    abstract fun mealDao(): MealDao
    abstract fun bazarDao(): BazarDao
    abstract fun depositDao(): DepositDao
    abstract fun fixedCostDao(): FixedCostDao
    abstract fun monthlyClosingDao(): MonthlyClosingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meal_manager_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
