package nl.vibe.magister

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import nl.vibe.magister.di.AppContainer
import nl.vibe.magister.liveupdate.LiveUpdateHelper

class MagisterApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.sessionStore.start()
        WorkManager.initialize(this, Configuration.Builder().build())
        LiveUpdateHelper.ensureChannel(this)
    }
}
