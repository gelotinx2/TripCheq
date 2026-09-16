package com.southboundstudios.tripcheq.presentation.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    query: String,
    label: String,
    suggestions: List<GeocodingFeature>,
    isDropdownExpanded: Boolean,
    onQueryChanged: (String) -> Unit,
    onSuggestionSelected: (GeocodingFeature) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = {},
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        if (suggestions.isNotEmpty() && isDropdownExpanded) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { /* Handle dismiss if needed */ }
            ) {
                suggestions.forEach { feature ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = feature.text,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = feature.place_name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { onSuggestionSelected(feature) }
                    )
                }
            }
        }
    }
}