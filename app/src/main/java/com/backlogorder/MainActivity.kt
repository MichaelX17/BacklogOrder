package com.backlogorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.backlogorder.domain.model.ThemeId
import com.backlogorder.ui.navigation.BacklogOrderNavHost
import com.backlogorder.ui.theme.BacklogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BacklogOrderApplication
        val container = app.container

        setContent {
            val themeId by container.settingsRepository.theme.collectAsStateWithLifecycle(ThemeId.VIOLET)
            val isDarkMode by container.settingsRepository.isDarkMode.collectAsStateWithLifecycle(true)
            val customColorHex by container.settingsRepository.customColorHex.collectAsStateWithLifecycle("7045F5")

            BacklogTheme(
                themeId = themeId,
                darkTheme = isDarkMode,
                customColorHex = customColorHex,
            ) {
                BacklogOrderApp()
            }
        }
    }
}

@Composable
fun BacklogOrderApp() {
    val navController = rememberNavController()
    BacklogOrderNavHost(navController = navController)
}
