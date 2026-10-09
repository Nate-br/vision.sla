package et.vbl.core.network.interceptor

import et.vbl.core.datastore.AuthTokenStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injects Authorization: Bearer <token> into outgoing HTTP requests.
 * Skips public authentication endpoints.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: AuthTokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Skip auth endpoints that don't need existing bearer token
        if (path.contains("v1/auth/otp/") || path.contains("v1/auth/pin/verify") || path.contains("v1/auth/refresh")) {
            return chain.proceed(request)
        }

        val token = runBlocking {
            tokenStorage.authState.firstOrNull()?.accessToken
        }

        val authenticatedRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
