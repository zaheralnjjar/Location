package com.directnumber.app.ui.components

import android.text.BidiFormatter
import android.text.TextDirectionHeuristics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.domain.model.NumberType
import com.directnumber.app.domain.model.PhoneNumberResult
import com.directnumber.app.domain.model.ValidationStatus

/** Wraps a phone number with Unicode directional isolates so it always reads correctly inside an RTL paragraph. */
private fun bidiSafe(text: String): String =
    BidiFormatter.getInstance().unicodeWrap(text, TextDirectionHeuristics.LTR)

@Composable
fun PhonePreviewCard(result: PhoneNumberResult, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.preview_title), style = MaterialTheme.typography.titleMedium)
                ValidationStatusChip(status = result.status)
            }

            if (result.status == ValidationStatus.VALID) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                PreviewRow(stringResource(R.string.preview_international), bidiSafe(result.internationalDisplay.orEmpty()))
                PreviewRow(stringResource(R.string.preview_e164), bidiSafe(result.e164.orEmpty()))
                PreviewRow(stringResource(R.string.preview_whatsapp_number), bidiSafe(result.whatsAppNumber.orEmpty()))
                PreviewRow(stringResource(R.string.preview_type), numberTypeLabel(result.numberType))
            }
        }
    }
}

@Composable
private fun numberTypeLabel(type: NumberType): String = when (type) {
    NumberType.MOBILE -> stringResource(R.string.type_mobile)
    NumberType.LANDLINE -> stringResource(R.string.type_landline)
    NumberType.UNKNOWN -> stringResource(R.string.type_unknown)
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, textDirection = TextDirection.Ltr),
        )
    }
}
