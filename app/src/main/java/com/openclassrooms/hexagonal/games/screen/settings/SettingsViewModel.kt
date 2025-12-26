package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.preferences.NotificationPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val notificationPreferencesManager: NotificationPreferencesManager
) : ViewModel() {

  /**
   * StateFlow that emits the current notification enabled state.
   */
  val notificationsEnabled: StateFlow<Boolean> =
    notificationPreferencesManager.notificationsEnabled.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = true
    )

  /**
   * Enables notifications for the application.
   */
  fun enableNotifications() {
    viewModelScope.launch {
      notificationPreferencesManager.enableNotifications()
    }
  }

  /**
   * Disables notifications for the application.
   */
  fun disableNotifications() {
    viewModelScope.launch {
      notificationPreferencesManager.disableNotifications()
    }
  }
}
