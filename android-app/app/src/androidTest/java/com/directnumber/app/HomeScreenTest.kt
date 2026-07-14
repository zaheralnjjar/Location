package com.directnumber.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.directnumber.app.ui.home.HomeScreen
import com.directnumber.app.ui.theme.DirectNumberTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests — require a connected device or emulator to run
 * (`./gradlew connectedAndroidTest`). They exercise HomeScreen end to end against the real
 * DirectNumberApp service locator, since no fakes/mocks are substituted here.
 */
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launchHomeScreen() {
        composeRule.activity.setContent {
            DirectNumberTheme {
                HomeScreen(onSettingsClick = {})
            }
        }
    }

    @Test
    fun enteringAValidArgentineNumberShowsValidStatusAndEnablesActions() {
        launchHomeScreen()

        composeRule.onNodeWithText("Enter phone number").performTextInput("011 15-2345-6789")

        composeRule.onNodeWithText("Valid number").assertExists()
        composeRule.onNodeWithText("Open in WhatsApp").assertIsEnabled()
    }

    @Test
    fun incompleteNumberKeepsActionsDisabled() {
        launchHomeScreen()

        composeRule.onNodeWithText("Enter phone number").performTextInput("123")

        composeRule.onNodeWithText("Incomplete number").assertExists()
        composeRule.onNodeWithText("Open in WhatsApp").assertIsNotEnabled()
    }

    @Test
    fun noInputShowsEmptyState() {
        launchHomeScreen()

        composeRule.onNodeWithText("No preview yet").assertExists()
    }

    @Test
    fun selectingUnitedStatesFromQuickPicksUpdatesSelectedCountry() {
        launchHomeScreen()

        composeRule.onNodeWithText("🇺🇸 +1").performClick()

        composeRule.onNodeWithText("United States (+1)").assertExists()
    }

    @Test
    fun openingAllCountriesShowsSearchField() {
        launchHomeScreen()

        composeRule.onNodeWithText("All countries").performClick()

        composeRule.onNodeWithText("Search by name or dial code").assertExists()
    }

    @Test
    fun searchingCountryPickerByDialCodeFiltersResults() {
        launchHomeScreen()

        composeRule.onNodeWithText("All countries").performClick()
        // Search by digits only (not the full "+962") so the query text can't collide with
        // the "Jordan" name or the "+962" trailing label shown in the filtered result row.
        composeRule.onNodeWithText("Search by name or dial code").performTextInput("962")

        composeRule.onNodeWithText("Jordan").assertExists()
    }
}
