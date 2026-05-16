package com.smartstudent.planner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_DARK_MODE        = booleanPreferencesKey("dark_mode")
        val KEY_LANGUAGE         = stringPreferencesKey("language")
        val KEY_ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
        val KEY_UNIVERSITY       = stringPreferencesKey("university")
        val KEY_FACULTY          = stringPreferencesKey("faculty")
        val KEY_YEAR_OF_STUDY    = stringPreferencesKey("year_of_study")
        val KEY_FCM_TOKEN        = stringPreferencesKey("fcm_token")
        val KEY_LAST_SYNC        = stringPreferencesKey("last_sync_ts")
    }

    // ─── Dark mode ────────────────────────────────────────────────────────────
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    // ─── Language ─────────────────────────────────────────────────────────────
    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }
    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    // ─── Onboarding ───────────────────────────────────────────────────────────
    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }
    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    // ─── Profile info ─────────────────────────────────────────────────────────
    val university: Flow<String> = context.dataStore.data.map { it[KEY_UNIVERSITY] ?: "" }
    val faculty: Flow<String>    = context.dataStore.data.map { it[KEY_FACULTY]    ?: "" }
    val yearOfStudy: Flow<String> = context.dataStore.data.map { it[KEY_YEAR_OF_STUDY] ?: "" }

    suspend fun saveProfileInfo(university: String, faculty: String, year: String) {
        context.dataStore.edit {
            it[KEY_UNIVERSITY]    = university
            it[KEY_FACULTY]       = faculty
            it[KEY_YEAR_OF_STUDY] = year
        }
    }

    // ─── FCM token ────────────────────────────────────────────────────────────
    val fcmToken: Flow<String> = context.dataStore.data.map { it[KEY_FCM_TOKEN] ?: "" }
    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { it[KEY_FCM_TOKEN] = token }
    }

    // ─── Last sync timestamp ─────────────────────────────────────────────────
    val lastSync: Flow<String> = context.dataStore.data.map { it[KEY_LAST_SYNC] ?: "Never" }
    suspend fun updateLastSync(ts: String) {
        context.dataStore.edit { it[KEY_LAST_SYNC] = ts }
    }
}
