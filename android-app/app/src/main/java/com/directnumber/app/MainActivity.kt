package com.directnumber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.directnumber.app.data.model.AppLanguageOption
import com.directnumber.app.data.model.AppSettings
import com.directnumber.app.data.model.ThemeMode
import com.directnumber.app.ui.navigation.DirectNumberNavGraph
import com.directnumber.app.ui.theme.DirectNumberTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        val app = application as DirectNumberApp
        lifecycleScope.launch {
            val settings = app.settingsRepository.settings.first()
            applyLanguage(settings.language)
            isReady = true
        }

        enableEdgeToEdge()

        setContent {
            val settings by app.settingsRepository.settings.collectAsState(initial = AppSettings())

            LaunchedEffect(settings.language) {
                applyLanguage(settings.language)
            }

            DirectNumberTheme(themeMode = settings.themeMode) {
                val isLightBackground = !resolveIsDark(settings.themeMode)
                val insetsController = remember {
                    WindowCompat.getInsetsController(window, window.decorView)
                }
                SideEffect {
                    insetsController.isAppearanceLightStatusBars = isLightBackground
                    insetsController.isAppearanceLightNavigationBars = isLightBackground
                }
                DirectNumberNavGraph()
            }
        }
    }

    private fun applyLanguage(option: AppLanguageOption) {
        val localeList = when (option) {
            AppLanguageOption.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguageOption.ARABIC -> LocaleListCompat.forLanguageTags("ar")
            AppLanguageOption.SPANISH -> LocaleListCompat.forLanguageTags("es")
            AppLanguageOption.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        }
        if (AppCompatDelegate.getApplicationLocales() != localeList) {
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}

@Composable
private fun resolveIsDark(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}
