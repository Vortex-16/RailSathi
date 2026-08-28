package com.example.data.sync

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SyncQueueEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.SyncPayloadItem
import com.example.data.remote.SyncRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SyncManager(private val database: AppDatabase) {
    private val TAG = "SyncManager"

    suspend fun queueOfflineOperation(idempotencyKey: String, operationType: String, payload: JSONObject) {
        val entity = SyncQueueEntity(
            idempotencyKey = idempotencyKey,
            operationType = operationType,
            payloadJson = payload.toString()
        )
        database.syncQueueDao().queueItem(entity)
        Log.d(TAG, "Queued offline sync item: $operationType with key $idempotencyKey")
    }

    suspend fun performSync(): Boolean = withContext(Dispatchers.IO) {
        val pending = database.syncQueueDao().getPendingSyncItems()
        if (pending.isEmpty()) {
            return@withContext true
        }

        Log.d(TAG, "Found ${pending.size} pending offline operations to sync")

        val payloadItems = pending.map { entity ->
            val map = mutableMapOf<String, String>()
            try {
                val json = JSONObject(entity.payloadJson)
                json.keys().forEach { key ->
                    map[key] = json.optString(key)
                }
            } catch (e: Exception) {
                map["raw"] = entity.payloadJson
            }

            SyncPayloadItem(
                idempotencyKey = entity.idempotencyKey,
                operationType = entity.operationType,
                payload = map
            )
        }

        try {
            val response = ApiClient.apiService.syncOfflineQueue(SyncRequest(items = payloadItems))
            if (response.isSuccessful && response.body()?.success == true) {
                pending.forEach { database.syncQueueDao().markSynced(it.idempotencyKey) }
                Log.d(TAG, "Successfully synced ${pending.size} items to Vercel Cloud")
                return@withContext true
            } else {
                Log.w(TAG, "Sync returned non-success response: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network unavailable or sync failed. Items will remain in offline queue.", e)
        }

        false
    }
}
