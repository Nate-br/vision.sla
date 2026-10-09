package et.vbl.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequest(
    val phoneNumber: String,
    val deviceFingerprint: String,
    val deviceModel: String? = null,
    val osVersion: String? = null
)

@Serializable
data class RequestOtpResponse(
    val message: String,
    val expiresAt: String,
    val cooldownSeconds: Int,
    val isNewDevice: Boolean,
    val devOtp: String? = null
)

@Serializable
data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String,
    val deviceFingerprint: String
)

@Serializable
data class SetupPinRequest(
    val pin: String
)

@Serializable
data class VerifyPinRequest(
    val phoneNumber: String,
    val pin: String,
    val deviceFingerprint: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
    val deviceFingerprint: String
)

@Serializable
data class AuthTokensResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val tokenType: String,
    val isPinSet: Boolean,
    val isNewDevice: Boolean? = null,
    val member: MemberResponse? = null
)

@Serializable
data class MemberResponse(
    val id: String,
    val phoneNumber: String,
    val role: String,
    val kycStatus: String
)

@Serializable
data class MessageResponse(
    val message: String
)
