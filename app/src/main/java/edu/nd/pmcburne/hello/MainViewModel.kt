package edu.nd.pmcburne.hello

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUIState(
    val locations: List<Location> = emptyList(),
    val allTags: List<String> = emptyList(),
    val selectedTag: String = "core",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    private val db = AppDatabase.getInstance(application)
    private val dao = db.locationDao()

    init {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Fetching locations from API...")
                val response = RetrofitInstance.api.getLocations()
                val parsed = parseLocations(response)
                Log.d("MainViewModel", "Parsed ${parsed.size} locations from API")

                val existingIds = dao.getLocationsOnce().map { it.id }.toSet()
                Log.d("MainViewModel", "Existing IDs in DB: $existingIds")

                val newLocations = parsed.filter { it.id !in existingIds }
                Log.d("MainViewModel", "New locations to insert: ${newLocations.size}")

                if (newLocations.isNotEmpty()) {
                    dao.upsertLocations(newLocations)
                    Log.d("MainViewModel", "Saved ${newLocations.size} new locations to DB")
                } else {
                    Log.d("MainViewModel", "No new locations, DB already up to date")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "API fetch failed: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }

            // Observe Room DB
            dao.getLocations().collect { locations ->
                Log.d("MainViewModel", "DB update: ${locations.size} locations")

                val allTags = locations
                    .flatMap { it.tags.split(",") }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()

                Log.d("MainViewModel", "All tags: $allTags")

                _uiState.update {
                    it.copy(
                        locations = locations,
                        allTags = allTags,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectTag(tag: String) {
        Log.d("MainViewModel", "Tag selected: $tag")
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun filteredLocations(): List<Location> {
        val state = _uiState.value
        val filtered = state.locations.filter { location ->
            location.tags.split(",").map { it.trim() }.contains(state.selectedTag)
        }
        Log.d("MainViewModel", "Filtered for '${state.selectedTag}': ${filtered.size} locations")
        return filtered
    }
}