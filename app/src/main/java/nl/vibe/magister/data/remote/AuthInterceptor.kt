package nl.vibe.magister.data.remote

import nl.vibe.magister.data.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionStore: SessionStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionStore.currentToken()
        val request = chain.request().newBuilder().apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        return chain.proceed(request)
    }
}
