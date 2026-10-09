package et.vbl.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import et.vbl.core.security.crypto.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vbl_auth_prefs")

data class AuthState(
    val accessToken: String?,
    val refreshToken: String?,
    val memberId: String?,
    val phoneNumber: String?,
    val isPinSet: Boolean,
    val isBiometricsEnabled: Boolean,
    val deviceFingerprint: String
)

@Singleton
class AuthTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    companion object {
        private val KEY_ACCESS_TOKEN_ENC = stringPreferencesKey("access_token_enc")
        private val KEY_REFRESH_TOKEN_ENC = stringPreferencesKey("refresh_token_enc")
        private val KEY_MEMBER_ID = stringPreferencesKey("member_id")
        private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val KEY_IS_PIN_SET = booleanPreferencesKey("is_pin_set")
        private val KEY_IS_BIOMETRICS_ENABLED = booleanPreferencesKey("is_biometrics_enabled")
        private val KEY_DEVICE_FINGERPRINT = stringPreferencesKey("device_fingerprint")
    }

    private fun encryptString(plain: String): String {
        val encrypted = cryptoManager.encrypt(plain.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun decryptString(encryptedBase64: String): String {
        val bytes = Base64.getDecoder().decode(encryptedBase64)
        return String(cryptoManager.decrypt(bytes), Charsets.UTF_8)
    }

    val authState: Flow<AuthState> = context.dataStore.data.map { prefs ->
        val accessEnc = prefs[KEY_ACCESS_TOKEN_ENC]
        val refreshEnc = prefs[KEY_REFRESH_TOKEN_ENC]

        val accessToken = accessEnc?.let { runCatching { decryptString(it) }.getOrNull() }
        val refreshToken = refreshEnc?.let { runCatching { decryptString(it) }.getOrNull() }

        AuthState(
            accessToken = accessToken,
            refreshToken = refreshToken,
            memberId = prefs[KEY_MEMBER_ID],
            phoneNumber = prefs[KEY_PHONE_NUMBER],
            isPinSet = prefs[KEY_IS_PIN_SET] ?: false,
            isBiometricsEnabled = prefs[KEY_IS_BIOMETRICS_ENABLED] ?: false,
            deviceFingerprint = prefs[KEY_DEVICE_FINGERPRINT] ?: getOrCreateDeviceFingerprint(prefs)
        )
    }

    private fun getOrCreateDeviceFingerprint(prefs: Preferences): String {
        return prefs[KEY_DEVICE_FINGERPRINT] ?: UUID.randomUUID().toString()
    }

    suspend fun saveAuthTokens(
        accessToken: String,
        refreshToken: String,
        memberId: String,
        phoneNumber: String,
        isPinSet: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN_ENC] = encryptString(accessToken)
            prefs[KEY_REFRESH_TOKEN_ENC] = encryptString(refreshToken)
            prefs[KEY_MEMBER_ID] = memberId
            prefs[KEY_PHONE_NUMBER] = phoneNumber
            prefs[KEY_IS_PIN_SET] = isPinSet
            if (!prefs.contains(KEY_DEVICE_FINGERPRINT)) {
                prefs[KEY_DEVICE_FINGERPRINT] = UUID.randomUUID().toString()
            }
        }
    }

    suspend fun updateAccessToken(newAccessToken: String, newRefreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN_ENC] = encryptString(newAccessToken)
            prefs[KEY_REFRESH_TOKEN_ENC] = encryptString(newRefreshToken)
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_BIOMETRICS_ENABLED] = enabled
        }
    }

    suspend fun setPinConfigured(isSet: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PIN_SET] = isSet
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN_ENC)
            prefs.remove(KEY_REFRESH_TOKEN_ENC)
            prefs.remove(KEY_MEMBER_ID)
            prefs.remove(KEY_IS_PIN_SET)
            prefs.remove(KEY_IS_BIOMETRICS_ENABLED)
        }
    }
}
