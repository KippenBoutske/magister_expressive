package nl.vibe.magister.util

import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val NL: Locale = Locale("nl", "NL")

val ZONE: ZoneId = ZoneId.of("Europe/Amsterdam")

fun now(): OffsetDateTime = OffsetDateTime.now(ZONE)

fun parseMagisterDate(raw: String?): OffsetDateTime? {
    if (raw.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(raw) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).atZone(ZONE).toOffsetDateTime() }.getOrNull()
        ?: runCatching { Instant.parse(raw).atZone(ZONE).toOffsetDateTime() }.getOrNull()
        ?: raw.toLongOrNull()?.let { Instant.ofEpochMilli(it).atZone(ZONE).toOffsetDateTime() }
}

fun OffsetDateTime.hhmm(): String = format(DateTimeFormatter.ofPattern("HH:mm"))

fun OffsetDateTime.dayLabel(): String =
    format(DateTimeFormatter.ofPattern("EEEE d MMMM", NL)).replaceFirstChar { it.uppercase(NL) }

fun OffsetDateTime.shortDay(): String =
    format(DateTimeFormatter.ofPattern("E d MMM", NL)).replaceFirstChar { it.uppercase(NL) }

fun OffsetDateTime.apiParam(): String = format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
