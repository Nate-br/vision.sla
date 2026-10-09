package et.vbl.core.security.ui

import android.app.Activity
import android.view.WindowManager

/**
 * Enforces FLAG_SECURE on screens displaying sensitive data (PIN, OTP, KYC, Receipts, Balances).
 * Prevents screen recording, screenshots, and OS recents thumbnails (OWASP MASVS-PLATFORM-2, S-113).
 */
fun Activity.setSecureScreen(enabled: Boolean) {
    if (enabled) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
