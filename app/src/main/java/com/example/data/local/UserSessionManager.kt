package com.example.data.local

import android.content.Context
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val email: String,
    val name: String,
    val username: String,
    val avatarUri: String? = null,
    val isLoggedIn: Boolean = false,
    val joinedDate: String = "September 2026",
    val studyStreakDays: Int = 4
)

class UserSessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("quicks_user_session", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    private fun loadProfile(): UserProfile {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val email = prefs.getString("user_email", "") ?: ""
        val name = prefs.getString("user_name", "") ?: ""
        val username = prefs.getString("user_username", "") ?: ""
        val avatarUri = prefs.getString("user_avatar", null)
        val joinedDate = prefs.getString("joined_date", "Active Member") ?: "Active Member"
        val streak = prefs.getInt("study_streak", 1)

        return UserProfile(
            email = email,
            name = name,
            username = username,
            avatarUri = avatarUri,
            isLoggedIn = isLoggedIn,
            joinedDate = joinedDate,
            studyStreakDays = streak
        )
    }

    private fun loadTheme(): AppThemeMode {
        val themeStr = prefs.getString("app_theme", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        return try {
            AppThemeMode.valueOf(themeStr)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }
    }

    fun saveProfile(name: String, username: String, email: String, avatarUri: String? = null) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_username", username)
            .putString("user_email", email)
            .putString("user_avatar", avatarUri)
            .apply()

        _userProfile.value = UserProfile(
            email = email,
            name = name,
            username = username,
            avatarUri = avatarUri,
            isLoggedIn = true,
            joinedDate = _userProfile.value.joinedDate,
            studyStreakDays = _userProfile.value.studyStreakDays
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("app_theme", mode.name).apply()
        _themeMode.value = mode
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _userProfile.value = _userProfile.value.copy(isLoggedIn = false)
    }
}
