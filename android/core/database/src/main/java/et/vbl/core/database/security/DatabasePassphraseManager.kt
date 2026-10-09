package et.vbl.core.database.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import et.vbl.core.security.crypto.CryptoManager
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the SQLCipher encryption passphrase backed by Android KeyStore (AES-256-GCM).
 * Ensures all local SQLite data is encrypted at rest (OWASP MASVS-STORAGE-1).
 */
@Singleton
class DatabasePassphraseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    companion object {
        private const val PREFS_NAME = "vbl_db_secure_prefs"
        private const val KEY_ENCRYPTED_PASSPHRASE = "enc_db_passphrase"
        private const val PASSPHRASE_LENGTH_BYTES = 32 // 256-bit key
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val existingEncryptedBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        if (existingEncryptedBase64 != null) {
            return try {
                val encryptedBytes = Base64.getDecoder().decode(existingEncryptedBase64)
                cryptoManager.decrypt(encryptedBytes)
            } catch (e: Exception) {
                // If corrupted or Keystore invalidated, generate fresh passphrase
                generateAndSavePassphrase()
            }
        }

        return generateAndSavePassphrase()
    }

    private fun generateAndSavePassphrase(): ByteArray {
        val randomBytes = ByteArray(PASSPHRASE_LENGTH_BYTES)
        SecureRandom().nextBytes(randomBytes)

        val encrypted = cryptoManager.encrypt(randomBytes)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encrypted)

        prefs.edit().putString(KEY_ENCRYPTED_PASSPHRASE, encryptedBase64).apply()
        return randomBytes
    }

    fun clearPassphrase() {
        prefs.edit().remove(KEY_ENCRYPTED_PASSPHRASE).apply()
    }
}
