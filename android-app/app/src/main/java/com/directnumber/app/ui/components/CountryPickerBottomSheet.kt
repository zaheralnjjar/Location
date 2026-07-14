package com.directnumber.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.domain.model.Country
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerBottomSheet(
    sheetState: SheetState,
    locale: Locale,
    recentRegionCodes: List<String>,
    searchResults: (String) -> List<Country>,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { searchResults(query) }
    val recentCountries = remember(recentRegionCodes, query) {
        if (query.isBlank()) recentRegionCodes.mapNotNull { code -> results.firstOrNull { it.regionCode == code } } else emptyList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.country_picker_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.country_picker_search_placeholder)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.cd_search)) },
                singleLine = true,
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (recentCountries.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.country_picker_recent),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(recentCountries, key = { "recent_${it.regionCode}" }) { country ->
                        CountryRow(country, locale) { onCountrySelected(country.regionCode) }
                    }
                }

                if (results.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(stringResource(R.string.country_picker_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    if (query.isBlank()) {
                        item {
                            Text(
                                stringResource(R.string.country_picker_all),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    items(results, key = { it.regionCode }) { country ->
                        CountryRow(country, locale) { onCountrySelected(country.regionCode) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryRow(country: Country, locale: Locale, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Text(country.flagEmoji, style = MaterialTheme.typography.titleLarge) },
        headlineContent = { Text(country.displayName(locale)) },
        trailingContent = {
            Text("+${country.dialCode}", style = TextStyle(textDirection = TextDirection.Ltr))
        },
    )
}
