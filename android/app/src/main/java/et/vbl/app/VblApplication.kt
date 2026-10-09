package et.vbl.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import et.vbl.core.common.logging.ReleaseTree
import timber.log.Timber

@HiltAndroidApp
class VblApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogging()
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            // Full debug logging — tags are stripped in release
            Timber.plant(Timber.DebugTree())
        } else {
            // Release tree is a no-op for all tags containing sensitive data.
            // Crashes are reported through Firebase Crashlytics (no PII).
            Timber.plant(ReleaseTree())
        }
    }
}
