package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FoodRequestEntity::class,
        VendorEntity::class,
        ExpenseEntity::class,
        SaleRecordEntity::class,
        JourneySessionEntity::class,
        OrderEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodRequestDao(): FoodRequestDao
    abstract fun vendorDao(): VendorDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun saleRecordDao(): SaleRecordDao
    abstract fun journeySessionDao(): JourneySessionDao
    abstract fun orderDao(): OrderDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "railsathi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
