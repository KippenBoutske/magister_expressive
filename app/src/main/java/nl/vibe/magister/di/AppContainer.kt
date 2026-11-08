package nl.vibe.magister.di

import android.content.Context
import com.google.gson.GsonBuilder
import nl.vibe.magister.data.remote.AuthInterceptor
import nl.vibe.magister.data.remote.MagisterApi
import nl.vibe.magister.data.remote.TokenAuthenticator
import nl.vibe.magister.data.repository.MagisterRepository
import nl.vibe.magister.data.session.SessionStore
import nl.vibe.magister.liveupdate.LiveUpdateHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    val sessionStore = SessionStore(appContext)

    private val gson = GsonBuilder().setLenient().create()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val plainClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val plainRetrofit = Retrofit.Builder()
        .baseUrl("https://www.magister.net/")
        .client(plainClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: MagisterApi = plainRetrofit.create(MagisterApi::class.java)

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionStore))
        .authenticator(TokenAuthenticator(sessionStore, authApi))
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.magister.net/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: MagisterApi = retrofit.create(MagisterApi::class.java)

    val repository = MagisterRepository(api, authApi, sessionStore)

    val liveUpdateHelper = LiveUpdateHelper(appContext)
}
