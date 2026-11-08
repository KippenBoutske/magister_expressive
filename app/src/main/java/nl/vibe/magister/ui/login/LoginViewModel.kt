package nl.vibe.magister.ui.login

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.vibe.magister.data.DemoData
import nl.vibe.magister.data.remote.MagisterConfig
import nl.vibe.magister.data.session.Session
import nl.vibe.magister.di.AppContainer
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

class LoginViewModel(private val container: AppContainer) : ViewModel() {

    var uiState by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    private var pendingTenant: String = ""
    private var codeVerifier: String = ""
    private var expectedState: String = ""

    fun authorizeUrl(tenant: String): String? {
        val t = tenant.trim()
        if (t.isBlank()) {
            uiState = LoginState.Error("Vul eerst je schoolnaam in")
            return null
        }
        pendingTenant = t
        codeVerifier = randomBase64Url(32)
        expectedState = randomBase64Url(16)
        return MagisterConfig.authorizeUrl(t, sha256Base64Url(codeVerifier), expectedState)
    }

    fun handleRedirect(url: String): Boolean {
        val uri = Uri.parse(url)
        if (uri.scheme != MagisterConfig.REDIRECT_SCHEME) return false

        val error = uri.getQueryParameter("error")
        if (error != null) {
            uiState = LoginState.Error(uri.getQueryParameter("error_description") ?: error)
            return true
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code == null || state != expectedState) {
            uiState = LoginState.Error("Ongeldige login-callback")
            return true
        }

        uiState = LoginState.Loading
        viewModelScope.launch {
            try {
                container.repository.exchangeCode(pendingTenant, code, codeVerifier)
                uiState = LoginState.Success
            } catch (e: Exception) {
                uiState = LoginState.Error(e.message ?: "Inloggen mislukt")
            }
        }
        return true
    }

    fun demo() {
        viewModelScope.launch {
            uiState = LoginState.Loading
            container.sessionStore.save(
                Session(
                    tenant = "demo",
                    accessToken = "",
                    refreshToken = "",
                    expiresAtEpochSec = Long.MAX_VALUE,
                    leerlingId = 1L,
                    name = DemoData.leerling.fullName,
                    demo = true
                )
            )
            uiState = LoginState.Success
        }
    }

    private fun randomBase64Url(size: Int): String {
        val bytes = ByteArray(size).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun sha256Base64Url(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data class Error(val message: String) : LoginState
    data object Success : LoginState
}
