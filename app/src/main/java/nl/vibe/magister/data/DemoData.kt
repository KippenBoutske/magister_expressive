package nl.vibe.magister.data

import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.data.model.Cijfer
import nl.vibe.magister.data.model.Leerling
import nl.vibe.magister.util.ZONE
import java.time.OffsetDateTime

object DemoData {

    val leerling = Leerling(
        id = 1L,
        roepnaam = "Demo",
        achternaam = "Leerling",
        officieleVoorletters = "D. Leerling"
    )

    val cijfers = listOf(
        Cijfer(id = 1L, kolomKop = "PW hfst 3", cijfer = "8,2", datumInvoer = "2025-09-12T10:00:00+02:00", vak = "Wiskunde", vakAfkorting = "wi", omschrijving = "Proefwerk hoofdstuk 3", weging = 3.0, geldig = true),
        Cijfer(id = 2L, kolomKop = "SO", cijfer = "6,4", datumInvoer = "2025-09-08T12:00:00+02:00", vak = "Wiskunde", vakAfkorting = "wi", omschrijving = "Schoolonderzoek", weging = 2.0, geldig = true),
        Cijfer(id = 3L, kolomKop = "SO", cijfer = "7,5", datumInvoer = "2025-09-15T09:00:00+02:00", vak = "Nederlands", vakAfkorting = "ne", omschrijving = "Boekbespreking", weging = 1.0, geldig = true),
        Cijfer(id = 4L, kolomKop = "PW", cijfer = "5,4", datumInvoer = "2025-09-02T14:00:00+02:00", vak = "Engels", vakAfkorting = "en", omschrijving = "Proefwerk unit 5", weging = 2.0, geldig = true),
        Cijfer(id = 5L, kolomKop = "SO", cijfer = "9,0", datumInvoer = "2025-09-18T11:00:00+02:00", vak = "Geschiedenis", vakAfkorting = "gs", omschrijving = "Schoolonderzoek", weging = 1.0, geldig = true),
        Cijfer(id = 6L, kolomKop = "PO", cijfer = "7,8", datumInvoer = "2025-09-20T13:00:00+02:00", vak = "Biologie", vakAfkorting = "bi", omschrijving = "Praktische opdracht", weging = 1.0, geldig = true)
    )

    fun afspraken(): List<Afspraak> {
        val now = OffsetDateTime.now(ZONE)
        val dayStart = now.toLocalDate().atStartOfDay(ZONE).toOffsetDateTime()

        fun les(id: Long, from: OffsetDateTime, minutes: Long, vak: String, docent: String, locatie: String, opdracht: String? = null) =
            Afspraak(
                id = id,
                start = from.toString(),
                einde = from.plusMinutes(minutes).toString(),
                omschrijving = vak,
                locatie = locatie,
                docent = docent,
                docentAfkorting = docent.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
                vak = vak,
                vakAfkorting = vak.take(2).lowercase(),
                type = if (opdracht != null) 103 else 1,
                afgerond = false,
                opdracht = opdracht,
                lesuur = id.toInt()
            )

        return listOf(
            les(1L, now.minusMinutes(12), 45, "Wiskunde", "P. Jansen", "A12"),
            les(2L, now.plusMinutes(40), 45, "Nederlands", "S. de Vries", "B04"),
            les(3L, now.plusMinutes(90), 45, "Engels", "M. Brown", "A12", opdracht = "Unit 5 vocabulaire leren, oefening 12 t/m 24"),
            les(4L, dayStart.plusDays(1).plusHours(8), 45, "Geschiedenis", "K. Bakker", "C21"),
            les(5L, dayStart.plusDays(1).plusHours(9), 90, "Biologie", "L. Peeters", "Lab 2", opdracht = "Practicumverslag inleveren v\u00f3\u00f3r vrijdag"),
            les(6L, dayStart.plusDays(2).plusHours(10), 45, "Aardrijkskunde", "R. Smit", "D10")
        )
    }
}
