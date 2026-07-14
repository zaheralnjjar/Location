package com.directnumber.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.data.model.WhatsAppTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppPickerDialog(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (target: WhatsAppTarget, rememberChoice: Boolean) -> Unit,
) {
    var rememberChoice by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            stringResource(R.string.whatsapp_picker_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.whatsapp_picker_regular)) },
            modifier = Modifier.clickable { onConfirm(WhatsAppTarget.REGULAR, rememberChoice) },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.whatsapp_picker_business)) },
            modifier = Modifier.clickable { onConfirm(WhatsAppTarget.BUSINESS, rememberChoice) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = rememberChoice, onCheckedChange = { rememberChoice = it })
            Text(stringResource(R.string.whatsapp_picker_remember))
        }
    }
}
