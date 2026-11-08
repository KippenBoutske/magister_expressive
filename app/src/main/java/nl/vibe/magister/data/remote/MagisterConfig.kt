package nl.vibe.magister.data.remote

import nl.vibe.magister.util.encode

object MagisterConfig {

    const val CLIENT_ID = "magister-expressive-android"
    const val REDIRECT_SCHEME = "magisterex"
    const val REDIRECT_URI = "magisterex://oauth/callback"
    const val SCOPE = "openid profile email"

    fun baseUrl(tenant: String): String = "https://${tenant.trim()}.magister.net"

    fun tokenUrl(tenant: String): String = "${baseUrl(tenant)}/api/token"

    fun authorizeUrl(tenant: String, codeChallenge: String, state: String): String =
        "${baseUrl(tenant)}/api/authorize" +
            "?response_type=code" +
            "&client_id=${encode(CLIENT_ID)}" +
            "&redirect_uri=${encode(REDIRECT_URI)}" +
            "&scope=${encode(SCOPE)}" +
            "&state=${encode(state)}" +
            "&code_challenge=${encode(codeChallenge)}" +
            "&code_challenge_method=S256"
}
