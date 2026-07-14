package com.directnumber.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.directnumber.app.R

@Composable
fun ShareVCardButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.Share, contentDescription = null)
        Text(stringResource(R.string.action_share_vcard), modifier = Modifier.padding(start = 8.dp))
    }
}
