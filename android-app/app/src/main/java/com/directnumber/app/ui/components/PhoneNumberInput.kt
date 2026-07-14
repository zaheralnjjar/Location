package com.directnumber.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import com.directnumber.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.phone_number_label)) },
        placeholder = { Text(stringResource(R.string.phone_number_placeholder)) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        trailingIcon = {
            Row {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cd_clear))
                    }
                }
                IconButton(onClick = onPasteClick) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = stringResource(R.string.cd_paste))
                }
            }
        },
    )
}
