package et.vbl.core.common.logging

import timber.log.Timber

/**
 * Release Timber tree.
 *
 * Rules (MASVS-RESILIENCE-2 / MASVS-STORAGE-2):
 * - Tags prefixed with "SECURE:" are suppressed entirely (use for auth, tokens, amounts, IDs).
 * - All other logs at ERROR level are forwarded to Firebase Crashlytics as non-fatal events.
 * - No PII (names, phone numbers, amounts, reference numbers) must appear in any log message.
 */
class ReleaseTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Suppress secure-tagged logs completely in release
        if (tag?.startsWith("SECURE:") == true) return false
        // Only log errors and above in release
        return priority >= android.util.Log.ERROR
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Forward to Crashlytics — message must never contain PII
        // FirebaseCrashlytics.getInstance().log("[$tag] $message")
        // t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        // TODO(Phase 1): uncomment after google-services.json is configured
    }
}
