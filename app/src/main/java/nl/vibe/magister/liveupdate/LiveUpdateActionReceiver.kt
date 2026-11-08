package nl.vibe.magister.liveupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LiveUpdateActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LiveUpdateHelper.ACTION_STOP) {
            LessonProgressWorker.cancel(context)
        }
    }
}
