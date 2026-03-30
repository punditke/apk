package com.xtremeiptv.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPlay: (String, String, String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    
    val channels by viewModel.channels.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val series by viewModel.series.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            viewModel.search(it)
                        },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            if (channels.isNotEmpty()) {
                item {
                    Text("Channels", style = MaterialTheme.typography.titleMedium)
                }
                items(channels) { channel ->
                    SearchResultItem(
                        title = channel.name,
                        subtitle = "Channel",
                        onClick = { onPlay(channel.id, "live", channel.name) }
                    )
                }
            }
            
            if (movies.isNotEmpty()) {
                item {
                    Text("Movies", style = MaterialTheme.typography.titleMedium)
                }
                items(movies) { movie ->
                    SearchResultItem(
                        title = movie.title,
                        subtitle = "Movie",
                        onClick = { onPlay(movie.id, "movie", movie.title) }
                    )
                }
            }
            
            if (series.isNotEmpty()) {
                item {
                    Text("Series", style = MaterialTheme.typography.titleMedium)
                }
                items(series) { seriesItem ->
                    SearchResultItem(
                        title = seriesItem.name,
                        subtitle = "Series",
                        onClick = { onPlay(seriesItem.id, "series", seriesItem.name) }
                    )
                }
            }
            
            if (query.isNotBlank() && channels.isEmpty() && movies.isEmpty() && series.isEmpty() && !isLoading) {
                item {
                    Text("No results found", modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
