package com.directnumber.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.directnumber.app.R
import com.directnumber.app.data.model.WhatsAppTarget

private fun WhatsAppTarget.label(): Int = when (this) {
    WhatsAppTarget.REGULAR -> R.string.whatsapp_picker_regular
    WhatsAppTarget.BUSINESS -> R.string.whatsapp_picker_business
    WhatsAppTarget.ASK_EVERY_TIME -> R.string.whatsapp_picker_regular
}

@Composable
fun AppNotInstalledDialog(
    missingTarget: WhatsAppTarget,
    fallbackTarget: WhatsAppTarget?,
    fallbackInstalled: Boolean,
    onOpenFallback: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onDismiss: () -> Unit,
) {
    val missingName = stringResource(missingTarget.label())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.app_not_installed_title, missingName)) },
        text = { Text(stringResource(R.string.app_not_installed_body)) },
        confirmButton = {
            TextButton(onClick = onOpenPlayStore) { Text(stringResource(R.string.app_not_installed_play_store)) }
        },
        dismissButton = {
            if (fallbackInstalled && fallbackTarget != null) {
                TextButton(onClick = onOpenFallback) {
                    Text(stringResource(R.string.app_not_installed_open_other, stringResource(fallbackTarget.label())))
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_not_installed_cancel)) }
            }
        },
    )
}

@Composable
fun BothWhatsAppMissingDialog(onOpenPlayStore: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error_title)) },
        text = { Text(stringResource(R.string.both_whatsapp_not_installed)) },
        confirmButton = {
            TextButton(onClick = onOpenPlayStore) { Text(stringResource(R.string.app_not_installed_play_store)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_not_installed_cancel)) }
        },
    )
}
