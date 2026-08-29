package com.example

/**
 * RailSaathi Central Application Configuration
 *
 * Update [API_BASE_URL] with your deployed Vercel URL (e.g. https://YOUR-PROJECT.vercel.app).
 * For local Android Emulator development against a local server, use "http://10.0.2.2:3000".
 */
object AppConfig {
    // Single central configuration point for the entire Android application
    const val API_BASE_URL: String = "https://railsaathi-backend-39959879941.asia-south1.run.app"

    // Default station geofence radius in meters (120m near station)
    const val STATION_GEOFENCE_METERS: Float = 1500f

    // Allowed unit price tiers that can be chosen by vendors
    val ALLOWED_UNIT_PRICES = listOf(5, 10, 15, 20, 30, 40, 50)
    const val MAX_UNIT_PRICE = 50
    const val MAX_ITEM_QUANTITY = 10
}
