package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("railsathi_prefs", Context.MODE_PRIVATE)

    private val _roleFlow = MutableStateFlow(getSavedRole())
    val roleFlow: StateFlow<UserRole?> = _roleFlow.asStateFlow()

    private val _languageFlow = MutableStateFlow(getSavedLanguage())
    val languageFlow: StateFlow<IndianLanguage> = _languageFlow.asStateFlow()

    private val _seniorModeFlow = MutableStateFlow(getSavedSeniorMode())
    val seniorModeFlow: StateFlow<Boolean> = _seniorModeFlow.asStateFlow()

    private val _onboardingCompletedFlow = MutableStateFlow(isOnboardingCompleted())
    val onboardingCompletedFlow: StateFlow<Boolean> = _onboardingCompletedFlow.asStateFlow()

    private val _journeyHintShownFlow = MutableStateFlow(isJourneyHintShown())
    val journeyHintShownFlow: StateFlow<Boolean> = _journeyHintShownFlow.asStateFlow()

    private val _foodHintShownFlow = MutableStateFlow(isFoodHintShown())
    val foodHintShownFlow: StateFlow<Boolean> = _foodHintShownFlow.asStateFlow()

    private val _requestHintShownFlow = MutableStateFlow(isRequestHintShown())
    val requestHintShownFlow: StateFlow<Boolean> = _requestHintShownFlow.asStateFlow()

    private val _vendorHintShownFlow = MutableStateFlow(isVendorHintShown())
    val vendorHintShownFlow: StateFlow<Boolean> = _vendorHintShownFlow.asStateFlow()

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompletedFlow.value = completed
    }

    fun isJourneyHintShown(): Boolean = prefs.getBoolean(KEY_JOURNEY_HINT_SHOWN, false)
    fun setJourneyHintShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_JOURNEY_HINT_SHOWN, shown).apply()
        _journeyHintShownFlow.value = shown
    }

    fun isFoodHintShown(): Boolean = prefs.getBoolean(KEY_FOOD_HINT_SHOWN, false)
    fun setFoodHintShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_FOOD_HINT_SHOWN, shown).apply()
        _foodHintShownFlow.value = shown
    }

    fun isRequestHintShown(): Boolean = prefs.getBoolean(KEY_REQUEST_HINT_SHOWN, false)
    fun setRequestHintShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_REQUEST_HINT_SHOWN, shown).apply()
        _requestHintShownFlow.value = shown
    }

    fun isVendorHintShown(): Boolean = prefs.getBoolean(KEY_VENDOR_HINT_SHOWN, false)
    fun setVendorHintShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_VENDOR_HINT_SHOWN, shown).apply()
        _vendorHintShownFlow.value = shown
    }

    fun resetAllHints() {
        prefs.edit()
            .putBoolean(KEY_JOURNEY_HINT_SHOWN, false)
            .putBoolean(KEY_FOOD_HINT_SHOWN, false)
            .putBoolean(KEY_REQUEST_HINT_SHOWN, false)
            .putBoolean(KEY_VENDOR_HINT_SHOWN, false)
            .apply()
        _journeyHintShownFlow.value = false
        _foodHintShownFlow.value = false
        _requestHintShownFlow.value = false
        _vendorHintShownFlow.value = false
    }

    fun getSavedRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun saveRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        _roleFlow.value = role
    }

    fun getSavedLanguage(): IndianLanguage {
        val code = prefs.getString(KEY_LANGUAGE_CODE, "hi") ?: "hi"
        return IndianLanguage.values().find { it.code == code } ?: IndianLanguage.HINDI
    }

    fun saveLanguage(language: IndianLanguage) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, language.code).apply()
        _languageFlow.value = language
    }

    fun getSavedSeniorMode(): Boolean {
        return prefs.getBoolean(KEY_SENIOR_MODE, false)
    }

    fun saveSeniorMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SENIOR_MODE, enabled).apply()
        _seniorModeFlow.value = enabled
    }

    fun getSavedTravelStatus(): com.example.data.location.UserTravelStatus {
        val name = prefs.getString(KEY_USER_TRAVEL_STATUS, "STATIONARY") ?: "STATIONARY"
        return try {
            com.example.data.location.UserTravelStatus.valueOf(name)
        } catch (_: Exception) {
            com.example.data.location.UserTravelStatus.STATIONARY
        }
    }

    fun saveTravelStatus(status: com.example.data.location.UserTravelStatus) {
        prefs.edit().putString(KEY_USER_TRAVEL_STATUS, status.name).apply()
    }

    fun getSavedLocationServicesEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCATION_SERVICES_ENABLED, true)
    }

    fun saveLocationServicesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_SERVICES_ENABLED, enabled).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _onboardingCompletedFlow.value = false
        _roleFlow.value = UserRole.GUEST
        _languageFlow.value = IndianLanguage.ENGLISH
        _seniorModeFlow.value = false
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_LANGUAGE_CODE = "key_language_code"
        private const val KEY_SENIOR_MODE = "key_senior_mode"
        private const val KEY_JOURNEY_HINT_SHOWN = "key_journey_hint_shown"
        private const val KEY_FOOD_HINT_SHOWN = "key_food_hint_shown"
        private const val KEY_REQUEST_HINT_SHOWN = "key_request_hint_shown"
        private const val KEY_VENDOR_HINT_SHOWN = "key_vendor_hint_shown"
        private const val KEY_USER_TRAVEL_STATUS = "key_user_travel_status"
        private const val KEY_LOCATION_SERVICES_ENABLED = "key_location_services_enabled"
    }
}
