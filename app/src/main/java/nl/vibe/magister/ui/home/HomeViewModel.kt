package nl.vibe.magister.ui.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.di.AppContainer
import nl.vibe.magister.liveupdate.LessonProgressWorker
import nl.vibe.magister.ui.UiState
import nl.vibe.magister.util.ZONE
import nl.vibe.magister.util.now
import nl.vibe.magister.util.parseMagisterDate
import java.time.Duration
import java.time.OffsetDateTime

class HomeViewModel(
    private val container: AppContainer,
    private val appContext: Context
) : ViewModel() {

    var uiState by mutableStateOf<UiState<List<Afspraak>>>(UiState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    var userName by mutableStateOf("")
        private set

    init {
        load()
        viewModelScope.launch {
            try {
                userName = container.repository.currentUser().fullName
            } catch (_: Exception) {
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            if (uiState !is UiState.Success) uiState = UiState.Loading
            refreshing = true
            try {
                val from = now().toLocalDate().atStartDay(ZONE).toOffsetDateTime()
                val to = from.plusDays(14)
                val list = container.repository.afspraken(from, to)
                uiState = UiState.Success(list)
                updateLiveUpdate(list)
            } catch (e: Exception) {
                if (uiState !is UiState.Success) {
                    uiState = UiState.Error(e.message ?: "Onbekende fout")
                }
            } finally {
                refreshing = false
            }
        }
    }

    fun currentLesson(data: List<Afspraak>, at: OffsetDateTime = now()): Afspraak? {
        return data.firstOrNull { afspraak ->
            val start = parseMagisterDate(afspraak.start)
            val end = parseMagisterDate(afspraak.einde)
            start != null && end != null && !at.isBefore(start) && at.isBefore(end)
        }
    }

    fun startLiveUpdate(afspraak: Afspraak, at: OffsetDateTime = now()) {
        val start = parseMagisterDate(afspraak.start) ?: return
        val end = parseMagisterDate(afspraak.einde) ?: return
        val totalMs = Duration.between(start, end).toMillis().coerceAtLeast(1L)
        val fraction = Duration.between(start, at).toMillis().toFloat() / totalMs.toFloat()
        val remainingMin = Duration.between(at, end).toMinutes().coerceAtLeast(0L)
        container.liveUpdateHelper.showLesson(afspraak, fraction, "nog $remainingMin min")
        LessonProgressWorker.schedule(appContext)
    }

    fun logout() {
        viewModelScope.launch {
            LessonProgressWorker.cancel(appContext)
            container.repository.logout()
        }
    }

    private fun updateLiveUpdate(list: List<Afspraak>) {
        val current = currentLesson(list)
        if (current != null) {
            startLiveUpdate(current)
        } else {
            container.liveUpdateHelper.cancel()
            LessonProgressWorker.schedule(appContext)
        }
    }
}
