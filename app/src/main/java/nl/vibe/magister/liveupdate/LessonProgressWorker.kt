package nl.vibe.magister.liveupdate

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import nl.vibe.magister.MagisterApp
import nl.vibe.magister.util.ZONE
import nl.vibe.magister.util.now
import nl.vibe.magister.util.parseMagisterDate
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class LessonProgressWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MagisterApp
        val session = app.container.sessionStore.session.value ?: return Result.success()

        val currentTime = now()
        val from = currentTime.toLocalDate().atStartOfDay(ZONE).toOffsetDateTime()
        val to = from.plusDays(1)

        val afspraken = runCatching { app.container.repository.afspraken(from, to) }
            .getOrDefault(emptyList())

        val current = afspraken.firstOrNull { afspraak ->
            val start = parseMagisterDate(afspraak.start)
            val end = parseMagisterDate(afspraak.einde)
            start != null && end != null &&
                !currentTime.isBefore(start) && currentTime.isBefore(end)
        }

        val helper = app.container.liveUpdateHelper
        if (current != null) {
            val start = parseMagisterDate(current.start)!!
            val end = parseMagisterDate(current.einde)!!
            val totalMs = Duration.between(start, end).toMillis().coerceAtLeast(1L)
            val elapsedMs = Duration.between(start, currentTime).toMillis()
            val fraction = elapsedMs.toFloat() / totalMs.toFloat()
            val remainingMin = Duration.between(currentTime, end).toMinutes()
            helper.showLesson(current, fraction, "nog $remainingMin min")
        } else {
            helper.cancel()
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "lesson_live_update"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LessonProgressWorker>(
                15,
                TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            LiveUpdateHelper(context).cancel()
        }
    }
}
