package com.directnumber.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.directnumber.app.R
import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.ui.components.AppNotInstalledDialog
import com.directnumber.app.ui.components.AppTopBar
import com.directnumber.app.ui.components.BothWhatsAppMissingDialog
import com.directnumber.app.ui.components.ContactNameInput
import com.directnumber.app.ui.components.CopyActionsSheet
import com.directnumber.app.ui.components.CountryPickerBottomSheet
import com.directnumber.app.ui.components.CountrySelector
import com.directnumber.app.ui.components.EmptyState
import com.directnumber.app.ui.components.ErrorDialog
import com.directnumber.app.ui.components.PhoneNumberInput
import com.directnumber.app.ui.components.PhonePreviewCard
import com.directnumber.app.ui.components.PreparedMessageInput
import com.directnumber.app.ui.components.PriorityCountriesRow
import com.directnumber.app.ui.components.RegionConflictBanner
import com.directnumber.app.ui.components.SaveContactButton
import com.directnumber.app.ui.components.ShareVCardButton
import com.directnumber.app.ui.components.WhatsAppActionButtons
import com.directnumber.app.ui.components.WhatsAppPickerDialog
import com.directnumber.app.ui.components.currentAppLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locale = currentAppLocale()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val defaultContactName = stringResource(R.string.default_contact_name)

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { event ->
            when (event) {
                is HomeSnackbarEvent.ResourceMessage ->
                    snackbarHostState.showSnackbar(context.getString(event.messageResId))
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(onSettingsClick = onSettingsClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ContactNameInput(value = uiState.contactName, onValueChange = viewModel::onContactNameChange)

            CountrySelector(
                selectedCountry = uiState.selectedCountry,
                locale = locale,
                onClick = viewModel::onOpenCountryPicker,
            )

            PriorityCountriesRow(
                countries = uiState.quickPickCountries,
                selectedRegionCode = uiState.selectedRegionCode,
                onCountrySelected = viewModel::onCountrySelected,
                onAllCountriesClick = viewModel::onOpenCountryPicker,
            )

            PhoneNumberInput(
                value = uiState.rawPhoneInput,
                onValueChange = viewModel::onPhoneInputChange,
                onPasteClick = { viewModel.onPasteRequested(context) },
                onClearClick = viewModel::onClearPhoneInput,
            )

            PreparedMessageInput(value = uiState.preparedMessage, onValueChange = viewModel::onPreparedMessageChange)

            val result = uiState.phoneResult
            if (result == null) {
                EmptyState()
            } else {
                if (result.hasRegionConflict) {
                    RegionConflictBanner(
                        onUseDetected = viewModel::onAcceptDetectedRegion,
                        onKeepSelected = viewModel::onKeepSelectedRegion,
                    )
                }
                AnimatedVisibility(visible = true) {
                    PhonePreviewCard(result = result)
                }
            }

            val actionsEnabled = result?.isActionable == true

            WhatsAppActionButtons(
                enabled = actionsEnabled,
                onOpenRegular = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onOpenWhatsAppClicked(context, WhatsAppTarget.REGULAR)
                },
                onOpenBusiness = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onOpenWhatsAppClicked(context, WhatsAppTarget.BUSINESS)
                },
            )

            SaveContactButton(enabled = actionsEnabled, onClick = { viewModel.onSaveContactClicked(context) })
            ShareVCardButton(
                enabled = actionsEnabled,
                onClick = { viewModel.onShareVCardClicked(context, defaultContactName) },
            )

            OutlinedButton(
                onClick = viewModel::onOpenCopySheet,
                enabled = actionsEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_copy_share))
            }
        }
    }

    when (uiState.activeSheet) {
        is HomeSheet.CountryPicker -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            CountryPickerBottomSheet(
                sheetState = sheetState,
                locale = locale,
                recentRegionCodes = uiState.recentRegionCodes,
                searchResults = { query -> viewModel.countriesForPicker(query, locale) },
                onCountrySelected = viewModel::onCountrySelected,
                onDismiss = viewModel::onDismissSheet,
            )
        }
        is HomeSheet.CopyActions -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            CopyActionsSheet(
                sheetState = sheetState,
                hasPreparedMessage = uiState.preparedMessage.isNotBlank(),
                onDismiss = viewModel::onDismissSheet,
                onCopy = { type -> viewModel.onCopyRequested(context, type) },
                onShareText = { withMessage -> viewModel.onShareTextRequested(context, withMessage) },
            )
        }
        is HomeSheet.WhatsAppPicker -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            WhatsAppPickerDialog(
                sheetState = sheetState,
                onDismiss = viewModel::onDismissSheet,
                onConfirm = { target, rememberChoice -> viewModel.onWhatsAppPickerConfirmed(context, target, rememberChoice) },
            )
        }
        null -> Unit
    }

    when (val dialog = uiState.activeDialog) {
        is HomeDialog.AppNotInstalled -> AppNotInstalledDialog(
            missingTarget = dialog.missingTarget,
            fallbackTarget = dialog.fallbackTarget,
            fallbackInstalled = dialog.fallbackInstalled,
            onOpenFallback = { viewModel.onOpenAlternativeWhatsApp(context) },
            onOpenPlayStore = { viewModel.onOpenPlayStoreForMissingApp(context) },
            onDismiss = viewModel::onDismissDialog,
        )
        is HomeDialog.BothWhatsAppMissing -> BothWhatsAppMissingDialog(
            onOpenPlayStore = { viewModel.onOpenPlayStoreForMissingApp(context) },
            onDismiss = viewModel::onDismissDialog,
        )
        is HomeDialog.GenericError -> ErrorDialog(
            message = stringResource(dialog.messageResId),
            onDismiss = viewModel::onDismissDialog,
        )
        null -> Unit
    }
}
