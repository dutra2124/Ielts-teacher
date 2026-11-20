package com.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.ai.configureAppCheck
import com.example.notifications.ApexNotificationHelper
import com.example.ui.ApexIeltsApp

class MainActivity : ComponentActivity() {

    private val pendingTabIndex = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        handleNotificationIntent(intent)
        applySystemBarsForTheme(isDark = false)

        setContent {
            ApexIeltsApp(
                deepLinkTabIndex = pendingTabIndex.value,
                onDeepLinkConsumed = { pendingTabIndex.value = null },
                onThemeDarknessChanged = { isDark -> applySystemBarsForTheme(isDark) }
            )
        }
    }

    private fun applySystemBarsForTheme(isDark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.hasExtra(ApexNotificationHelper.EXTRA_TARGET_TAB_INDEX) == true) {
            val tabIdx = intent.getIntExtra(ApexNotificationHelper.EXTRA_TARGET_TAB_INDEX, -1)
            if (tabIdx >= 0) {
                pendingTabIndex.value = tabIdx
            }
        }
    }
}
