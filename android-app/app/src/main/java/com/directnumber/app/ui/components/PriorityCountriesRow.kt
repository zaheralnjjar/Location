package com.directnumber.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.directnumber.app.R
import com.directnumber.app.domain.model.Country
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityCountriesRow(
    countries: List<Country>,
    selectedRegionCode: String,
    onCountrySelected: (String) -> Unit,
    onAllCountriesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(countries, key = { it.regionCode }) { country ->
            FilterChip(
                selected = country.regionCode == selectedRegionCode,
                onClick = { onCountrySelected(country.regionCode) },
                label = {
                    Text(
                        "${country.flagEmoji} +${country.dialCode}",
                        style = TextStyle(textDirection = TextDirection.Ltr),
                    )
                },
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = onAllCountriesClick,
                label = { Text(stringResource(R.string.all_countries_button)) },
            )
        }
    }
}

@Composable
fun currentAppLocale(): Locale {
    val configuration = LocalConfiguration.current
    return configuration.locales.get(0)
}
