package com.directnumber.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.ui.home.CopyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyActionsSheet(
    sheetState: SheetState,
    hasPreparedMessage: Boolean,
    onDismiss: () -> Unit,
    onCopy: (CopyType) -> Unit,
    onShareText: (withMessage: Boolean) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            stringResource(R.string.copy_sheet_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.copy_international)) },
            leadingContent = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
            modifier = Modifier.clickable { onCopy(CopyType.INTERNATIONAL) },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.copy_whatsapp_number)) },
            leadingContent = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
            modifier = Modifier.clickable { onCopy(CopyType.WHATSAPP) },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.copy_e164)) },
            leadingContent = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
            modifier = Modifier.clickable { onCopy(CopyType.E164) },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.share_as_text)) },
            leadingContent = { Icon(Icons.Outlined.Share, contentDescription = null) },
            modifier = Modifier.clickable { onShareText(false) },
        )
        if (hasPreparedMessage) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.share_with_message)) },
                leadingContent = { Icon(Icons.Outlined.Share, contentDescription = null) },
                modifier = Modifier.clickable { onShareText(true) },
            )
        }
    }
}
