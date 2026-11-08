package nl.vibe.magister.data.repository

import nl.vibe.magister.data.DemoData
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.data.model.Cijfer
import nl.vibe.magister.data.model.Leerling
import nl.vibe.magister.data.remote.MagisterApi
import nl.vibe.magister.data.remote.MagisterConfig
import nl.vibe.magister.data.session.Session
import nl.vibe.magister.data.session.SessionStore
import nl.vibe.magister.util.apiParam
import nl.vibe.magister.util.encode
import nl.vibe.magister.util.parseMagisterDate
import java.time.OffsetDateTime

class MagisterRepository(
    private val api: MagisterApi,
    private val authApi: MagisterApi,
    private val sessionStore: SessionStore
) {

    suspend fun exchangeCode(tenant: String, code: String, codeVerifier: String) {
        val tokens = authApi.token(
            url = MagisterConfig.tokenUrl(tenant),
            grantType = "authorization_code",
            code = code,
            redirectUri = MagisterConfig.REDIRECT_URI,
            clientId = MagisterConfig.CLIENT_ID,
            codeVerifier = codeVerifier
        )
        val accessToken = tokens.accessToken
        require(!accessToken.isNullOrBlank()) { "Geen access token ontvangen van Magister" }

        val expiresAt = System.currentTimeMillis() / 1000 + (tokens.expiresIn ?: 3600)
        sessionStore.save(
            Session(
                tenant = tenant.trim(),
                accessToken = accessToken,
                refreshToken = tokens.refreshToken.orEmpty(),
                expiresAtEpochSec = expiresAt,
                leerlingId = 0L,
                name = "",
                demo = false
            )
        )

        val leerling = currentUser()
        sessionStore.updateProfile(leerling.id ?: 0L, leerling.fullName)
    }

    suspend fun refreshIfNeeded() {
        val s = sessionStore.session.value ?: return
        if (s.demo) return
        if (s.expiresAtEpochSec - 60 > System.currentTimeMillis() / 1000) return
        val tokens = authApi.token(
            url = MagisterConfig.tokenUrl(s.tenant),
            grantType = "refresh_token",
            refreshToken = s.refreshToken,
            clientId = MagisterConfig.CLIENT_ID
        )
        if (!tokens.accessToken.isNullOrBlank()) {
            sessionStore.updateTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken ?: s.refreshToken,
                expiresAtEpochSec = System.currentTimeMillis() / 1000 + (tokens.expiresIn ?: 3600)
            )
        }
    }

    suspend fun currentUser(): Leerling {
        val s = requireSession()
        if (s.demo) return DemoData.leerling
        refreshIfNeeded()
        return api.currentUser("${MagisterConfig.baseUrl(s.tenant)}/api/v1/leerling")
    }

    suspend fun afspraken(from: OffsetDateTime, to: OffsetDateTime): List<Afspraak> {
        val s = requireSession()
        if (s.demo) return DemoData.afspraken()
        refreshIfNeeded()
        val url = MagisterConfig.baseUrl(s.tenant) +
            "/api/v1/leerlingen/${s.leerlingId}/afspraken" +
            "?van=${encode(from.apiParam())}&tot=${encode(to.apiParam())}"
        return api.afspraken(url).sortedBy { parseMagisterDate(it.start) }
    }

    suspend fun homework(from: OffsetDateTime, to: OffsetDateTime): List<Afspraak> =
        afspraken(from, to).filter { it.isHomework }

    suspend fun cijfers(): List<Cijfer> {
        val s = requireSession()
        if (s.demo) return DemoData.cijfers
        refreshIfNeeded()
        val base = "${MagisterConfig.baseUrl(s.tenant)}/api/v1/leerlingen/${s.leerlingId}"
        val kolommen = api.cijferKolommen("$base/cijfers/kolommen?actief=true")
        return kolommen
            .filter { it.actief != false }
            .flatMap { kolom ->
                val kolomId = kolom.kolomId ?: kolom.id ?: return@flatMap emptyList<Cijfer>()
                api.cijfers("$base/cijfers?kolomId=$kolomId")
                    .filter { it.geldig != false }
                    .sortedByDescending { parseMagisterDate(it.datumInvoer) }
            }
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    private fun requireSession(): Session =
        sessionStore.session.value ?: throw IllegalStateException("Niet ingelogd")
}
