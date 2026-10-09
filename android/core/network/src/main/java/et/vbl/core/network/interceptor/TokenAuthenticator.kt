package et.vbl.core.network.interceptor

import et.vbl.core.datastore.AuthTokenStorage
import et.vbl.core.network.api.AuthApiService
import et.vbl.core.network.model.RefreshTokenRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Handles HTTP 401 Unauthorized by executing a synchronized, single-flight token refresh.
 * Aligned with OWASP MASVS-AUTH-3 and item S-108.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: AuthTokenStorage,
    private val authApiProvider: Provider<AuthApiService>
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val originalRequest = response.request

        // Avoid infinite retry loop if refresh itself failed with 401
        if (originalRequest.url.encodedPath.contains("v1/auth/refresh")) {
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                val currentAuthState = tokenStorage.authState.firstOrNull()
                val currentAccessToken = currentAuthState?.accessToken
                val refreshToken = currentAuthState?.refreshToken

                // If another thread already refreshed the token, retry with updated token immediately
                val headerToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
                if (currentAccessToken != null && headerToken != null && currentAccessToken != headerToken) {
                    return@runBlocking originalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }

                if (refreshToken.isNullOrBlank()) {
                    tokenStorage.clearSession()
                    return@runBlocking null
                }

                try {
                    val authApi = authApiProvider.get()
                    val refreshResponse = authApi.refreshToken(
                        RefreshTokenRequest(
                            refreshToken = refreshToken,
                            deviceFingerprint = currentAuthState.deviceFingerprint
                        )
                    )

                    if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                        val body = refreshResponse.body()!!
                        tokenStorage.updateAccessToken(body.accessToken, body.refreshToken)

                        return@runBlocking originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${body.accessToken}")
                            .build()
                    } else {
                        // Refresh token rejected, expired, or reuse detected: terminate session
                        tokenStorage.clearSession()
                        return@runBlocking null
                    }
                } catch (e: Exception) {
                    tokenStorage.clearSession()
                    return@runBlocking null
                }
            }
        }
    }
}
