package com.southboundstudios.tripcheq.presentation.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature

@Composable
fun LocationSearchBar(
    query: String,
    label: String,
    isLocked: Boolean,
    suggestions: List<GeocodingFeature>,
    isShowingSuggestions: Boolean,
    onQueryChanged: (String) -> Unit,
    onSuggestionSelected: (GeocodingFeature) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isLocked) {
            OutlinedTextField(
                value = query,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Locked Location", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                label = { Text(label) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Text")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(visible = isShowingSuggestions && suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    LazyColumn {
                        items(suggestions) { feature ->
                            ListItem(
                                headlineContent = { Text(feature.text) },
                                supportingContent = { Text(feature.place_name) },
                                modifier = Modifier.clickable { onSuggestionSelected(feature) },
                            )
                        }
                    }
                }
            }
        }
    }
}