package nl.vibe.magister.data.remote

import kotlinx.coroutines.runBlocking
import nl.vibe.magister.data.session.SessionStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionStore: SessionStore,
    private val authApi: MagisterApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        val session = sessionStore.session.value ?: return null
        if (session.demo) return null

        val refreshed = runBlocking {
            runCatching {
                authApi.token(
                    url = MagisterConfig.tokenUrl(session.tenant),
                    grantType = "refresh_token",
                    refreshToken = session.refreshToken,
                    clientId = MagisterConfig.CLIENT_ID
                )
            }.getOrNull()
        } ?: return null

        val newAccess = refreshed.accessToken ?: return null
        val expiresAt = System.currentTimeMillis() / 1000 + (refreshed.expiresIn ?: 3600)
        runBlocking {
            sessionStore.updateTokens(
                accessToken = newAccess,
                refreshToken = refreshed.refreshToken ?: session.refreshToken,
                expiresAtEpochSec = expiresAt
            )
        }

        val newHeader = "Bearer $newAccess"
        if (response.request.header("Authorization") == newHeader) return null
        return response.request.newBuilder().header("Authorization", newHeader).build()
    }
}
