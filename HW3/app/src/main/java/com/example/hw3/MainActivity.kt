package com.example.hw3

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hw3.notifications.NotificationConstants
import com.example.hw3.ui.LocationScreen
import com.example.hw3.ui.NotificationScreen
import com.example.hw3.ui.PermissionDeniedScreen
import com.example.hw3.ui.theme.HW3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.refreshNotificationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            requestNotificationPermissionIfNeeded()
            // Приложение было закрыто и открыто по нажатию на уведомление -> заголовок
            showToastFromIntent(intent, NotificationConstants.EXTRA_TITLE)
        }

        // Список активных уведомлений обновляется, пока приложение на экране
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    viewModel.refreshActiveNotifications()
                    delay(NotificationConstants.ACTIVE_POLL_INTERVAL_MS)
                }
            }
        }

        setContent {
            HW3Theme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            !viewModel.hasNotificationPermission -> PermissionDeniedScreen(
                                onOpenSettings = { openNotificationSettings() },
                            )
                            viewModel.currentScreen == Screen.LOCATION ->
                                LocationScreen(viewModel = viewModel)
                            else -> NotificationScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Приложение уже было запущено и открыто по нажатию на уведомление -> текст
        showToastFromIntent(intent, NotificationConstants.EXTRA_TEXT)
    }

    override fun onResume() {
        super.onResume()
        // Пользователь мог выдать разрешение в настройках и вернуться в приложение
        viewModel.refreshNotificationPermission()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !viewModel.hasNotificationPermission
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showToastFromIntent(intent: Intent?, extraKey: String) {
        val message = intent?.getStringExtra(extraKey) ?: return
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(PACKAGE_URI_SCHEME, packageName, null),
            )
        }
        startActivity(intent)
    }

    private companion object {
        const val PACKAGE_URI_SCHEME = "package"
    }
}