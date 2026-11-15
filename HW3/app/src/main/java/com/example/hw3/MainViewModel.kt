package com.example.hw3

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.hw3.location.LocationHelper
import com.example.hw3.location.LocationState
import com.example.hw3.notifications.NotificationHelper
import com.example.hw3.notifications.NotificationImportance

enum class Screen { MAIN, LOCATION }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationHelper = NotificationHelper(application)
    private val locationHelper = LocationHelper(application)

    var currentScreen by mutableStateOf(Screen.MAIN)
        private set
    var hasNotificationPermission by mutableStateOf(notificationHelper.hasPermission())
        private set
    var activeIds by mutableStateOf<List<Int>>(emptyList())
        private set
    var locationState by mutableStateOf<LocationState>(LocationState.Hint)
        private set

    var title by mutableStateOf("")
    var text by mutableStateOf("")
    var isNonCancellable by mutableStateOf(false)
    var importance by mutableStateOf(NotificationImportance.DEFAULT)
    var editId by mutableStateOf("")

    fun openScreen(screen: Screen) {
        currentScreen = screen
    }

    fun refreshNotificationPermission() {
        hasNotificationPermission = notificationHelper.hasPermission()
    }

    fun refreshActiveNotifications() {
        activeIds = notificationHelper.getActiveIds()
    }

    /** Возвращает id строки с ошибкой или null, если всё прошло успешно. */
    fun sendNotification(): Int? {
        if (title.isBlank() || text.isBlank()) return R.string.error_empty_fields
        notificationHelper.show(title.trim(), text.trim(), importance, !isNonCancellable)
        refreshActiveNotifications()
        return null
    }

    /** Возвращает id строки с ошибкой или null, если всё прошло успешно. */
    fun editNotification(): Int? {
        refreshActiveNotifications()
        val id = editId.trim().toIntOrNull() ?: return R.string.error_invalid_id
        if (title.isBlank() || text.isBlank()) return R.string.error_empty_fields
        val isUpdated = notificationHelper.update(id, title.trim(), text.trim())
        refreshActiveNotifications()
        return if (isUpdated) null else R.string.error_notification_not_found
    }

    fun deleteAllNotifications() {
        notificationHelper.cancelAll()
        editId = ""
        refreshActiveNotifications()
    }

    fun loadLocation() {
        locationState = LocationState.Loading
        locationHelper.requestCurrentLocation { location ->
            locationState = if (location != null) {
                LocationState.Result(location.latitude, location.longitude)
            } else {
                LocationState.Unavailable
            }
        }
    }

    fun onLocationPermissionDenied() {
        locationState = LocationState.PermissionDenied
    }

    fun hasLocationPermission(): Boolean = locationHelper.hasPermission()
}