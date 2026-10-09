package et.vbl.core.network.api

import et.vbl.core.network.model.AuthTokensResponse
import et.vbl.core.network.model.MessageResponse
import et.vbl.core.network.model.RefreshTokenRequest
import et.vbl.core.network.model.RequestOtpRequest
import et.vbl.core.network.model.RequestOtpResponse
import et.vbl.core.network.model.SetupPinRequest
import et.vbl.core.network.model.VerifyOtpRequest
import et.vbl.core.network.model.VerifyPinRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("v1/auth/otp/request")
    suspend fun requestOtp(@Body request: RequestOtpRequest): Response<RequestOtpResponse>

    @POST("v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthTokensResponse>

    @POST("v1/auth/pin/setup")
    suspend fun setupPin(@Body request: SetupPinRequest): Response<MessageResponse>

    @POST("v1/auth/pin/verify")
    suspend fun verifyPin(@Body request: VerifyPinRequest): Response<AuthTokensResponse>

    @POST("v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthTokensResponse>

    @POST("v1/auth/logout")
    suspend fun logout(): Response<MessageResponse>
}
