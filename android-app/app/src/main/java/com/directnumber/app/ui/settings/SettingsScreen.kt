package com.directnumber.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.directnumber.app.BuildConfig
import com.directnumber.app.R
import com.directnumber.app.data.model.AppLanguageOption
import com.directnumber.app.data.model.ThemeMode
import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.ui.components.CountryPickerBottomSheet
import com.directnumber.app.ui.components.SettingItem
import com.directnumber.app.ui.components.currentAppLocale
import kotlinx.coroutines.launch

private enum class SettingsDialog { WHATSAPP_APP, LANGUAGE, THEME, DEFAULT_COUNTRY, CLEAR_HISTORY_CONFIRM, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsState()
    val locale = currentAppLocale()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf(SettingsDialog.NONE) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { resId ->
            scope.launch { snackbarHostState.showSnackbar(context.getString(resId)) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingItem(
                title = stringResource(R.string.settings_default_country),
                subtitle = regionDisplayName(settings.defaultRegionCode, locale),
                onClick = { activeDialog = SettingsDialog.DEFAULT_COUNTRY },
            )
            SettingItem(
                title = stringResource(R.string.settings_whatsapp_app),
                subtitle = whatsAppTargetLabel(settings.whatsAppTarget),
                onClick = { activeDialog = SettingsDialog.WHATSAPP_APP },
            )
            SettingItem(
                title = stringResource(R.string.settings_language),
                subtitle = languageLabel(settings.language),
                onClick = { activeDialog = SettingsDialog.LANGUAGE },
            )
            SettingItem(
                title = stringResource(R.string.settings_theme),
                subtitle = themeLabel(settings.themeMode),
                onClick = { activeDialog = SettingsDialog.THEME },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem(
                title = stringResource(R.string.settings_remember_last_country),
                trailing = {
                    Switch(checked = settings.rememberLastCountry, onCheckedChange = viewModel::setRememberLastCountry)
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_show_recent_countries),
                trailing = {
                    Switch(checked = settings.showRecentCountries, onCheckedChange = viewModel::setShowRecentCountries)
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_keep_history),
                subtitle = stringResource(R.string.settings_keep_history_desc),
                trailing = {
                    Switch(checked = settings.keepRecentNumbersHistory, onCheckedChange = viewModel::setKeepRecentNumbersHistory)
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_clear_history),
                onClick = { activeDialog = SettingsDialog.CLEAR_HISTORY_CONFIRM },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem(title = stringResource(R.string.settings_about), subtitle = stringResource(R.string.settings_about_body))
            SettingItem(title = stringResource(R.string.settings_privacy), subtitle = stringResource(R.string.settings_privacy_body))
            SettingItem(title = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME))
        }
    }

    when (activeDialog) {
        SettingsDialog.WHATSAPP_APP -> RadioOptionDialog(
            title = stringResource(R.string.settings_whatsapp_app),
            options = listOf(
                WhatsAppTarget.REGULAR to stringResource(R.string.settings_whatsapp_regular),
                WhatsAppTarget.BUSINESS to stringResource(R.string.settings_whatsapp_business),
                WhatsAppTarget.ASK_EVERY_TIME to stringResource(R.string.settings_whatsapp_ask),
            ),
            selected = settings.whatsAppTarget,
            onSelect = { viewModel.setWhatsAppTarget(it); activeDialog = SettingsDialog.NONE },
            onDismiss = { activeDialog = SettingsDialog.NONE },
        )
        SettingsDialog.LANGUAGE -> RadioOptionDialog(
            title = stringResource(R.string.settings_language),
            options = listOf(
                AppLanguageOption.SYSTEM to stringResource(R.string.settings_language_system),
                AppLanguageOption.ARABIC to stringResource(R.string.settings_language_ar),
                AppLanguageOption.SPANISH to stringResource(R.string.settings_language_es),
                AppLanguageOption.ENGLISH to stringResource(R.string.settings_language_en),
            ),
            selected = settings.language,
            onSelect = { viewModel.setLanguage(it); activeDialog = SettingsDialog.NONE },
            onDismiss = { activeDialog = SettingsDialog.NONE },
        )
        SettingsDialog.THEME -> RadioOptionDialog(
            title = stringResource(R.string.settings_theme),
            options = listOf(
                ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
            ),
            selected = settings.themeMode,
            onSelect = { viewModel.setThemeMode(it); activeDialog = SettingsDialog.NONE },
            onDismiss = { activeDialog = SettingsDialog.NONE },
        )
        SettingsDialog.DEFAULT_COUNTRY -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            CountryPickerBottomSheet(
                sheetState = sheetState,
                locale = locale,
                recentRegionCodes = emptyList(),
                searchResults = { query -> viewModel.countriesForPicker(query, locale) },
                onCountrySelected = { regionCode ->
                    viewModel.setDefaultRegion(regionCode)
                    activeDialog = SettingsDialog.NONE
                },
                onDismiss = { activeDialog = SettingsDialog.NONE },
            )
        }
        SettingsDialog.CLEAR_HISTORY_CONFIRM -> AlertDialog(
            onDismissRequest = { activeDialog = SettingsDialog.NONE },
            title = { Text(stringResource(R.string.settings_clear_history)) },
            text = { Text(stringResource(R.string.settings_keep_history_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearRecentNumbersHistory()
                    activeDialog = SettingsDialog.NONE
                }) { Text(stringResource(R.string.settings_clear_history)) }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = SettingsDialog.NONE }) {
                    Text(stringResource(R.string.app_not_installed_cancel))
                }
            },
        )
        SettingsDialog.NONE -> Unit
    }
}

@Composable
private fun <T> RadioOptionDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = value == selected, onClick = { onSelect(value) })
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = { onSelect(value) })
                        Text(label, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_not_installed_cancel)) }
        },
    )
}

@Composable
private fun whatsAppTargetLabel(target: WhatsAppTarget): String = when (target) {
    WhatsAppTarget.REGULAR -> stringResource(R.string.settings_whatsapp_regular)
    WhatsAppTarget.BUSINESS -> stringResource(R.string.settings_whatsapp_business)
    WhatsAppTarget.ASK_EVERY_TIME -> stringResource(R.string.settings_whatsapp_ask)
}

@Composable
private fun languageLabel(language: AppLanguageOption): String = when (language) {
    AppLanguageOption.SYSTEM -> stringResource(R.string.settings_language_system)
    AppLanguageOption.ARABIC -> stringResource(R.string.settings_language_ar)
    AppLanguageOption.SPANISH -> stringResource(R.string.settings_language_es)
    AppLanguageOption.ENGLISH -> stringResource(R.string.settings_language_en)
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun regionDisplayName(regionCode: String, locale: java.util.Locale): String =
    java.util.Locale.Builder().setRegion(regionCode).build().getDisplayCountry(locale)
