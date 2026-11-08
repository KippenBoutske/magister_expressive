package nl.vibe.magister.data.remote

import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.data.model.Cijfer
import nl.vibe.magister.data.model.CijferKolom
import nl.vibe.magister.data.model.Leerling
import nl.vibe.magister.data.model.TokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface MagisterApi {

    @FormUrlEncoded
    @POST
    suspend fun token(
        @Url url: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String? = null,
        @Field("redirect_uri") redirectUri: String? = null,
        @Field("client_id") clientId: String? = null,
        @Field("code_verifier") codeVerifier: String? = null,
        @Field("refresh_token") refreshToken: String? = null
    ): TokenResponse

    @GET
    suspend fun currentUser(@Url url: String): Leerling

    @GET
    suspend fun afspraken(@Url url: String): List<Afspraak>

    @GET
    suspend fun cijferKolommen(@Url url: String): List<CijferKolom>

    @GET
    suspend fun cijfers(@Url url: String): List<Cijfer>
}
