package nl.vibe.magister.ui.grades

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.vibe.magister.data.model.Cijfer
import nl.vibe.magister.di.AppContainer
import nl.vibe.magister.ui.UiState

class GradesViewModel(private val container: AppContainer) : ViewModel() {

    var uiState by mutableStateOf<UiState<List<Cijfer>>>(UiState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            if (uiState !is UiState.Success) uiState = UiState.Loading
            refreshing = true
            try {
                uiState = UiState.Success(container.repository.cijfers())
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Fout bij ophalen van cijfers")
            } finally {
                refreshing = false
            }
        }
    }
}
