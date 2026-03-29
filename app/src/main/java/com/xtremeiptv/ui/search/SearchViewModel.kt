package com.xtremeiptv.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import com.xtremeiptv.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()
    
    private val _movies = MutableStateFlow<List<VodItem>>(emptyList())
    val movies: StateFlow<List<VodItem>> = _movies.asStateFlow()
    
    private val _series = MutableStateFlow<List<Series>>(emptyList())
    val series: StateFlow<List<Series>> = _series.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var searchJob: kotlinx.coroutines.Job? = null
    
    fun search(query: String) {
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _channels.value = emptyList()
            _movies.value = emptyList()
            _series.value = emptyList()
            return
        }
        
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(300) // Debounce
            
            // In real implementation, search through actual content
            // For now, mock results
            _channels.value = listOf(
                Channel(id = "1", name = "BBC $query", streamUrl = ""),
                Channel(id = "2", name = "CNN $query", streamUrl = "")
            ).filter { it.name.contains(query, ignoreCase = true) }
            
            _movies.value = listOf(
                VodItem(id = "1", title = "Movie $query", streamUrl = "")
            ).filter { it.title.contains(query, ignoreCase = true) }
            
            _series.value = listOf(
                Series(id = "1", name = "Series $query")
            ).filter { it.name.contains(query, ignoreCase = true) }
            
            _isLoading.value = false
        }
    }
}
