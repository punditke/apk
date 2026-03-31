package com.xtremeiptv.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.VodItem
import com.xtremeiptv.data.repository.ContentRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    private val _movies = MutableStateFlow<List<VodItem>>(emptyList())
    val movies: StateFlow<List<VodItem>> = _movies.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentProfileId: String? = null
    
    init {
        loadMovies()
    }
    
    fun loadMovies(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val profile = profileRepository.getActiveProfile().firstOrNull()
                if (profile == null) {
                    _error.value = "No active profile"
                    _isLoading.value = false
                    return@launch
                }
                
                currentProfileId = profile.id
                val result = contentRepository.loadMovies(profile, useCache = !forceRefresh)
                
                _movies.value = result
                
                if (result.isEmpty() && !forceRefresh) {
                    loadMovies(forceRefresh = true)
                } else if (result.isEmpty()) {
                    _error.value = "No movies found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load movies"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadMovies(forceRefresh = true)
    }
    
    fun clearError() {
        _error.value = null
    }
}
