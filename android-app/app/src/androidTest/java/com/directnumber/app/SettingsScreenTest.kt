package com.directnumber.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.directnumber.app.ui.settings.SettingsScreen
import com.directnumber.app.ui.theme.DirectNumberTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launchSettingsScreen() {
        composeRule.activity.setContent {
            DirectNumberTheme {
                SettingsScreen(onBackClick = {})
            }
        }
    }

    @Test
    fun openingLanguageSettingShowsAllThreeLanguagesPlusSystem() {
        launchSettingsScreen()

        composeRule.onNodeWithText("App language").performClick()

        composeRule.onNodeWithText("System language").assertExists()
        composeRule.onNodeWithText("العربية").assertExists()
        composeRule.onNodeWithText("Español").assertExists()
        composeRule.onNodeWithText("English").assertExists()
    }

    @Test
    fun openingThemeSettingShowsLightDarkAndSystemOptions() {
        launchSettingsScreen()

        composeRule.onNodeWithText("Appearance").performClick()

        composeRule.onNodeWithText("Light").assertExists()
        composeRule.onNodeWithText("Dark").assertExists()
        composeRule.onNodeWithText("System default").assertExists()
    }

    @Test
    fun openingWhatsAppDefaultShowsThreeModes() {
        launchSettingsScreen()

        composeRule.onNodeWithText("Default WhatsApp app").performClick()

        composeRule.onNodeWithText("WhatsApp Business").assertExists()
        composeRule.onNodeWithText("Ask every time").assertExists()
    }
}
