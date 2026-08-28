package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getActiveUserDirect(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface FoodRequestDao {
    @Query("SELECT * FROM food_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM food_requests WHERE status = 'PENDING' OR status = 'ASSIGNED' OR status = 'IN_TRANSIT' ORDER BY timestamp DESC")
    fun getActiveRequests(): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM food_requests WHERE coachNumber = :coachNumber AND (status = 'PENDING' OR status = 'ASSIGNED' OR status = 'IN_TRANSIT')")
    fun getActiveRequestsByCoach(coachNumber: String): Flow<List<FoodRequestEntity>>

    @Query("SELECT * FROM food_requests WHERE assignedVendorId = :vendorId ORDER BY timestamp DESC")
    fun getRequestsForVendor(vendorId: String): Flow<List<FoodRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: FoodRequestEntity): Long

    @Update
    suspend fun updateRequest(request: FoodRequestEntity)

    @Query("UPDATE food_requests SET status = :status, assignedVendorId = :vendorId, assignedVendorName = :vendorName WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String, vendorId: String?, vendorName: String?)

    @Query("DELETE FROM food_requests WHERE id = :id")
    suspend fun deleteRequest(id: Long)
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE vendorId = :vendorId LIMIT 1")
    fun getVendorById(vendorId: String): Flow<VendorEntity?>

    @Query("SELECT * FROM vendors WHERE vendorId = :vendorId LIMIT 1")
    suspend fun getVendorByIdDirect(vendorId: String): VendorEntity?

    @Query("SELECT * FROM vendors WHERE currentTrain = :trainNumber AND currentCoach = :coachNumber")
    suspend fun getVendorsInCoach(trainNumber: String, coachNumber: String): List<VendorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Query("UPDATE vendors SET currentCoach = :coachNumber WHERE vendorId = :vendorId")
    suspend fun updateVendorCoach(vendorId: String, coachNumber: String)

    @Query("UPDATE vendors SET todaySalesCount = todaySalesCount + 1, todayEarnings = todayEarnings + :amount, lastSaleTimestamp = :timestamp WHERE vendorId = :vendorId")
    suspend fun recordVendorSale(vendorId: String, amount: Double, timestamp: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpent(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}

@Dao
interface SaleRecordDao {
    @Query("SELECT * FROM sale_records WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    fun getSalesByVendor(vendorId: String): Flow<List<SaleRecordEntity>>

    @Query("SELECT SUM(amount) FROM sale_records WHERE vendorId = :vendorId")
    fun getTotalVendorSales(vendorId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleRecord(record: SaleRecordEntity): Long
}

@Dao
interface JourneySessionDao {
    @Query("SELECT * FROM journey_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    fun getActiveJourney(): Flow<JourneySessionEntity?>

    @Query("SELECT * FROM journey_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveJourneyDirect(): JourneySessionEntity?

    @Query("SELECT * FROM journey_sessions ORDER BY startedAt DESC LIMIT 1")
    fun getLatestJourney(): Flow<JourneySessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneySessionEntity)

    @Update
    suspend fun updateJourney(journey: JourneySessionEntity)

    @Query("UPDATE journey_sessions SET status = 'COMPLETED', endedAt = :endedAt WHERE journeyId = :journeyId")
    suspend fun completeJourney(journeyId: String, endedAt: Long = System.currentTimeMillis())

    @Query("UPDATE journey_sessions SET status = 'CANCELLED', endedAt = :endedAt WHERE journeyId = :journeyId")
    suspend fun cancelJourney(journeyId: String, endedAt: Long = System.currentTimeMillis())

    @Query("UPDATE journey_sessions SET status = 'COMPLETED', endedAt = :endedAt WHERE status = 'ACTIVE'")
    suspend fun completeAllActiveJourneys(endedAt: Long = System.currentTimeMillis())

    @Query("UPDATE journey_sessions SET currentCoach = :coach WHERE journeyId = :journeyId")
    suspend fun updateCoach(journeyId: String, coach: String)

    @Query("UPDATE journey_sessions SET currentStation = :station, confidence = :confidence, lastLocationUpdate = :timestamp WHERE journeyId = :journeyId")
    suspend fun updateProgress(journeyId: String, station: String, confidence: Int, timestamp: Long)

    @Query("DELETE FROM journey_sessions WHERE status != 'ACTIVE'")
    suspend fun cleanOldJourneys()
}

