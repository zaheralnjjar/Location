package com.directnumber.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.domain.model.ValidationStatus
import com.directnumber.app.ui.theme.DirectNumberStatus

@Composable
fun ValidationStatusChip(status: ValidationStatus, modifier: Modifier = Modifier) {
    val statusColors = DirectNumberStatus.colors
    val (containerColor, contentColor, icon, textRes) = when (status) {
        ValidationStatus.VALID -> StatusVisuals(
            statusColors.successContainer, statusColors.success, Icons.Filled.CheckCircle, R.string.status_valid,
        )
        ValidationStatus.INCOMPLETE -> StatusVisuals(
            MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Filled.Info, R.string.status_incomplete,
        )
        ValidationStatus.INVALID -> StatusVisuals(
            MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, Icons.Filled.Error, R.string.status_invalid,
        )
        ValidationStatus.NEEDS_REVIEW -> StatusVisuals(
            statusColors.warningContainer, statusColors.warning, Icons.Filled.HelpOutline, R.string.status_needs_review,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(textRes), color = contentColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private data class StatusVisuals(
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val icon: ImageVector,
    val textRes: Int,
)

@Composable
fun RegionConflictBanner(
    onUseDetected: () -> Unit,
    onKeepSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColors = DirectNumberStatus.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = statusColors.warningContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.region_conflict_title),
                style = MaterialTheme.typography.titleMedium,
                color = statusColors.warning,
            )
            Text(
                stringResource(R.string.region_conflict_message),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onKeepSelected) {
                    Text(stringResource(R.string.region_conflict_keep_selected))
                }
                OutlinedButton(onClick = onUseDetected) {
                    Text(stringResource(R.string.region_conflict_use_detected))
                }
            }
        }
    }
}
