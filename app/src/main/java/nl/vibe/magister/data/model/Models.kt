package nl.vibe.magister.data.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("scope") val scope: String? = null
)

data class Leerling(
    @SerializedName("Id") val id: Long? = null,
    @SerializedName("Roepnaam") val roepnaam: String? = null,
    @SerializedName("Achternaam") val achternaam: String? = null,
    @SerializedName("OfficieleVoorletters") val officieleVoorletters: String? = null,
    @SerializedName("Email") val email: String? = null
) {
    val fullName: String
        get() = listOfNotNull(roepnaam, achternaam)
            .joinToString(" ")
            .ifBlank { officieleVoorletters.orEmpty() }
}

data class Afspraak(
    @SerializedName("Id") val id: Long? = null,
    @SerializedName("Start") val start: String? = null,
    @SerializedName("Einde") val einde: String? = null,
    @SerializedName("Omschrijving") val omschrijving: String? = null,
    @SerializedName("Locatie") val locatie: String? = null,
    @SerializedName("Docent") val docent: String? = null,
    @SerializedName("DocentAfkorting") val docentAfkorting: String? = null,
    @SerializedName("Vak") val vak: String? = null,
    @SerializedName("VakAfkorting") val vakAfkorting: String? = null,
    @SerializedName("GroepOmschrijving") val groepOmschrijving: String? = null,
    @SerializedName("Type") val type: Int? = null,
    @SerializedName("Status") val status: Int? = null,
    @SerializedName("Afgerond") val afgerond: Boolean? = null,
    @SerializedName("Opdracht") val opdracht: String? = null,
    @SerializedName("Instructie") val instructie: String? = null,
    @SerializedName("Lesuur") val lesuur: Int? = null
) {
    val isHomework: Boolean
        get() = !opdracht.isNullOrBlank() || type == 103
}

data class CijferKolom(
    @SerializedName("Id") val id: Long? = null,
    @SerializedName("KolomId") val kolomId: Long? = null,
    @SerializedName("KolomKop") val kolomKop: String? = null,
    @SerializedName("KolomNummer") val kolomNummer: Int? = null,
    @SerializedName("Vak") val vak: String? = null,
    @SerializedName("VakAfkorting") val vakAfkorting: String? = null,
    @SerializedName("Omschrijving") val omschrijving: String? = null,
    @SerializedName("Periode") val periode: String? = null,
    @SerializedName("Actief") val actief: Boolean? = null
)

data class Cijfer(
    @SerializedName("Id") val id: Long? = null,
    @SerializedName("KolomId") val kolomId: Long? = null,
    @SerializedName("KolomKop") val kolomKop: String? = null,
    @SerializedName("Cijfer") val cijfer: String? = null,
    @SerializedName("CijferStr") val cijferStr: String? = null,
    @SerializedName("DatumInvoer") val datumInvoer: String? = null,
    @SerializedName("Vak") val vak: String? = null,
    @SerializedName("VakAfkorting") val vakAfkorting: String? = null,
    @SerializedName("Omschrijving") val omschrijving: String? = null,
    @SerializedName("Weging") val weging: Double? = null,
    @SerializedName("Geldig") val geldig: Boolean? = null
) {
    val value: Double?
        get() = (cijfer ?: cijferStr)?.replace(',', '.')?.toDoubleOrNull()

    val display: String
        get() = (cijfer ?: cijferStr ?: "-").replace('.', ',')
}
