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

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompletedFlow.value = completed
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

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_LANGUAGE_CODE = "key_language_code"
        private const val KEY_SENIOR_MODE = "key_senior_mode"
    }
}
