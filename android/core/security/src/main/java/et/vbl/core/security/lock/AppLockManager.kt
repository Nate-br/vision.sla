package et.vbl.core.security.lock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enforces automatic app lockout after 60 seconds in the background (OWASP MASVS-AUTH-4, S-111).
 */
@Singleton
class AppLockManager @Inject constructor() {

    companion object {
        const val LOCKOUT_TIMEOUT_MS = 60_000L // 60 seconds
    }

    private var backgroundTimestamp: Long = 0L

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun onAppEnteredBackground() {
        backgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppEnteredForeground(hasActiveSession: Boolean) {
        if (!hasActiveSession) return

        if (backgroundTimestamp != 0L) {
            val elapsed = System.currentTimeMillis() - backgroundTimestamp
            if (elapsed >= LOCKOUT_TIMEOUT_MS) {
                _isLocked.value = true
            }
        }
        backgroundTimestamp = 0L
    }

    fun unlock() {
        _isLocked.value = false
        backgroundTimestamp = 0L
    }

    fun lockImmediately() {
        _isLocked.value = true
    }
}
