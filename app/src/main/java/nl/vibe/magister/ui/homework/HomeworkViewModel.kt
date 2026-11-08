package nl.vibe.magister.ui.homework

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.data.session.HomeworkPrefs
import nl.vibe.magister.di.AppContainer
import nl.vibe.magister.ui.UiState
import nl.vibe.magister.util.ZONE
import nl.vibe.magister.util.now

class HomeworkViewModel(
    private val container: AppContainer,
    appContext: Context
) : ViewModel() {

    private val prefs = HomeworkPrefs(appContext)

    var uiState by mutableStateOf<UiState<List<Afspraak>>>(UiState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    var doneIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    init {
        load()
        viewModelScope.launch {
            prefs.doneIds.collect { doneIds = it }
        }
    }

    fun load() {
        viewModelScope.launch {
            if (uiState !is UiState.Success) uiState = UiState.Loading
            refreshing = true
            try {
                val from = now().toLocalDate().atStartDay(ZONE).toOffsetDateTime()
                val to = from.plusDays(14)
                uiState = UiState.Success(container.repository.homework(from, to))
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Fout bij ophalen van huiswerk")
            } finally {
                refreshing = false
            }
        }
    }

    fun toggle(afspraak: Afspraak) {
        val id = afspraak.id ?: return
        viewModelScope.launch {
            prefs.setDone(id, id !in doneIds)
        }
    }
}
