package com.openclassrooms.hexagonal.games.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for notification preferences using DataStore.
 * Provides methods to enable/disable notifications and observe the current state.
 */
@Singleton
class NotificationPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

    companion object {
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    /**
     * Flow that emits the current notification enabled state.
     * Defaults to true if no preference has been set.
     */
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true // Default to enabled
    }

    /**
     * Enables notifications by saving the preference.
     */
    suspend fun enableNotifications() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = true
        }
    }

    /**
     * Disables notifications by saving the preference.
     */
    suspend fun disableNotifications() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = false
        }
    }

    /**
     * Gets the current notification enabled state synchronously.
     * This is a convenience method for non-suspend contexts.
     */
    suspend fun areNotificationsEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
        }.first()
    }
}
