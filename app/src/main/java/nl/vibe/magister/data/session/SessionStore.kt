package nl.vibe.magister.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

data class Session(
    val tenant: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSec: Long,
    val leerlingId: Long,
    val name: String,
    val demo: Boolean
)

class SessionStore(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val session = MutableStateFlow<Session?>(null)

    fun start() {
        scope.launch {
            session.value = context.sessionDataStore.data.first().toSession()
        }
    }

    suspend fun save(s: Session) {
        context.sessionDataStore.edit { p ->
            p[Keys.TENANT] = s.tenant
            p[Keys.ACCESS_TOKEN] = s.accessToken
            p[Keys.REFRESH_TOKEN] = s.refreshToken
            p[Keys.EXPIRES_AT] = s.expiresAtEpochSec
            p[Keys.LEERLING_ID] = s.leerlingId
            p[Keys.NAME] = s.name
            p[Keys.DEMO] = s.demo
        }
        session.value = s
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String, expiresAtEpochSec: Long) {
        val current = session.value ?: return
        context.sessionDataStore.edit { p ->
            p[Keys.ACCESS_TOKEN] = accessToken
            p[Keys.REFRESH_TOKEN] = refreshToken
            p[Keys.EXPIRES_AT] = expiresAtEpochSec
        }
        session.value = current.copy(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSec = expiresAtEpochSec
        )
    }

    suspend fun updateProfile(leerlingId: Long, name: String) {
        val current = session.value ?: return
        context.sessionDataStore.edit { p ->
            p[Keys.LEERLING_ID] = leerlingId
            p[Keys.NAME] = name
        }
        session.value = current.copy(leerlingId = leerlingId, name = name)
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
        session.value = null
    }

    fun currentToken(): String? = session.value?.accessToken?.ifBlank { null }

    private fun Preferences.toSession(): Session? {
        val tenant = this[Keys.TENANT] ?: return null
        return Session(
            tenant = tenant,
            accessToken = this[Keys.ACCESS_TOKEN].orEmpty(),
            refreshToken = this[Keys.REFRESH_TOKEN].orEmpty(),
            expiresAtEpochSec = this[Keys.EXPIRES_AT] ?: 0L,
            leerlingId = this[Keys.LEERLING_ID] ?: 0L,
            name = this[Keys.NAME].orEmpty(),
            demo = this[Keys.DEMO] ?: false
        )
    }

    private object Keys {
        val TENANT = stringPreferencesKey("tenant")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val LEERLING_ID = longPreferencesKey("leerling_id")
        val NAME = stringPreferencesKey("name")
        val DEMO = booleanPreferencesKey("demo")
    }
}
