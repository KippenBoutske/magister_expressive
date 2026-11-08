package nl.vibe.magister.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.homeworkDataStore: DataStore<Preferences> by preferencesDataStore(name = "homework")

class HomeworkPrefs(context: Context) {

    private val store = context.applicationContext.homeworkDataStore

    val doneIds: Flow<Set<Long>> = store.data.map { prefs ->
        (prefs[KEY] ?: emptySet()).mapNotNull { it.toLongOrNull() }.toSet()
    }

    suspend fun setDone(id: Long, done: Boolean) {
        store.edit { prefs ->
            val current = (prefs[KEY] ?: emptySet()).toMutableSet()
            if (done) current.add(id.toString()) else current.remove(id.toString())
            prefs[KEY] = current
        }
    }

    private companion object {
        val KEY = stringSetPreferencesKey("done_ids")
    }
}
