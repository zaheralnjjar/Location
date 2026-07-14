package com.directnumber.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.directnumber.app.R

@Composable
fun WhatsAppActionButtons(
    enabled: Boolean,
    onOpenRegular: () -> Unit,
    onOpenBusiness: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onOpenRegular, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Chat, contentDescription = null)
            Text(stringResource(R.string.action_open_whatsapp), modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = onOpenBusiness, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Chat, contentDescription = null)
            Text(stringResource(R.string.action_open_whatsapp_business), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
