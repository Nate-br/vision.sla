package et.vbl.core.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles AES-256-GCM encryption and decryption backed by the hardware Android KeyStore.
 * Aligned with OWASP MASVS-STORAGE-1.
 */
@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS = "vbl_master_keystore_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_IV_LENGTH_BYTES = 12
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Master data storage encryption
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(keyGenSpec)
        }.generateKey()
    }

    /**
     * Encrypts plaintext bytes. Returns [IV (12 bytes) + Ciphertext].
     */
    fun encrypt(plainBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainBytes)

        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return combined
    }

    /**
     * Decrypts combined [IV + Ciphertext] bytes back to plaintext.
     */
    fun decrypt(encryptedBytes: ByteArray): ByteArray {
        if (encryptedBytes.size < GCM_IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Invalid encrypted payload: too short")
        }

        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        val cipherBytes = ByteArray(encryptedBytes.size - GCM_IV_LENGTH_BYTES)

        System.arraycopy(encryptedBytes, 0, iv, 0, GCM_IV_LENGTH_BYTES)
        System.arraycopy(encryptedBytes, GCM_IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

        return cipher.doFinal(cipherBytes)
    }
}
