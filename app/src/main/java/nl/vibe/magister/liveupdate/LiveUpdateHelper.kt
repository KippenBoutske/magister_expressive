package nl.vibe.magister.liveupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import nl.vibe.magister.MainActivity
import nl.vibe.magister.R
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.util.hhmm
import nl.vibe.magister.util.parseMagisterDate

class LiveUpdateHelper(private val context: Context) {

    fun showLesson(afspraak: Afspraak, progressFraction: Float, remainingText: String) {
        val start = parseMagisterDate(afspraak.start)
        val end = parseMagisterDate(afspraak.einde)

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, LiveUpdateActionReceiver::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = (progressFraction.coerceIn(0f, 1f) * 100).toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lesson)
            .setContentTitle(afspraak.vak ?: afspraak.omschrijving ?: context.getString(R.string.lesson_label))
            .setContentText(
                buildString {
                    if (start != null) append(start.hhmm())
                    if (end != null) append(" - ${end.hhmm()}")
                    if (!afspraak.locatie.isNullOrBlank()) append(" \u2022 ${afspraak.locatie}")
                }
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(openIntent)
            .setDeleteIntent(stopIntent)
            .addAction(0, context.getString(R.string.open_app), openIntent)
            .addAction(0, context.getString(R.string.hide), stopIntent)
            .setShortCriticalText(remainingText)
            .setRequestPromotedOngoing(true)
            .setStyle(
                NotificationCompat.ProgressStyle()
                    .setProgress(100, progress, false)
                    .setProgressSegments(listOf(NotificationCompat.ProgressStyle.Segment(100)))
            )

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "live_updates"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "nl.vibe.magister.action.STOP_LIVE_UPDATE"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.live_update_channel),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.live_update_channel_desc)
                }
                NotificationManagerCompat.from(context).createNotificationChannel(channel)
            }
        }
    }
}
