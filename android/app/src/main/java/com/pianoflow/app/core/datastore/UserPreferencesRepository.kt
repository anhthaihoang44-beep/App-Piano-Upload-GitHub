package com.pianoflow.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pianoflow.app.core.audio.NotationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val NOTATION_TYPE = stringPreferencesKey("notation_type")
        val SUSTAIN_ENABLED = booleanPreferencesKey("sustain_enabled")
        val VOLUME = floatPreferencesKey("volume")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SKILL_LEVEL = stringPreferencesKey("skill_level")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_PRACTICE_DAY = stringPreferencesKey("last_practice_day")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val VISIBLE_KEYS_COUNT = intPreferencesKey("visible_keys_count")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val notationType: Flow<NotationType> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.NOTATION_TYPE]) {
            NotationType.DO_RE_MI.name -> NotationType.DO_RE_MI
            NotationType.OFF.name -> NotationType.OFF
            else -> NotationType.CDE
        }
    }

    val sustainEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SUSTAIN_ENABLED] ?: false }
    val volume: Flow<Float> = context.dataStore.data.map { it[Keys.VOLUME] ?: 0.9f }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAPTICS_ENABLED] ?: true }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    val skillLevel: Flow<String> = context.dataStore.data.map { it[Keys.SKILL_LEVEL] ?: "Beginner" }
    val dailyGoalMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.DAILY_GOAL_MINUTES] ?: 15 }
    val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.DAILY_REMINDER_ENABLED] ?: true }
    val streakDays: Flow<Int> = context.dataStore.data.map { it[Keys.STREAK_DAYS] ?: 1 }
    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_PREMIUM] ?: false }
    val visibleKeysCount: Flow<Int> = context.dataStore.data.map { it[Keys.VISIBLE_KEYS_COUNT] ?: 14 } // ~2 octaves default
    val appLanguage: Flow<String> = context.dataStore.data.map { it[Keys.APP_LANGUAGE] ?: "en" }

    suspend fun setNotationType(type: NotationType) {
        context.dataStore.edit { it[Keys.NOTATION_TYPE] = type.name }
    }

    suspend fun setSustainEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SUSTAIN_ENABLED] = enabled }
    }

    suspend fun setVolume(vol: Float) {
        context.dataStore.edit { it[Keys.VOLUME] = vol.coerceIn(0f, 1f) }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSkillLevel(level: String) {
        context.dataStore.edit { it[Keys.SKILL_LEVEL] = level }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MINUTES] = minutes }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setStreakDays(days: Int) {
        context.dataStore.edit { it[Keys.STREAK_DAYS] = days }
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { it[Keys.IS_PREMIUM] = premium }
    }

    suspend fun setVisibleKeysCount(count: Int) {
        context.dataStore.edit { it[Keys.VISIBLE_KEYS_COUNT] = count.coerceIn(7, 28) }
    }

    suspend fun setAppLanguage(languageCode: String) {
        val normalized = if (languageCode == "vi") "vi" else "en"
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = normalized }
    }
}
