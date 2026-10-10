package et.vbl.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpVerificationScreen(
    phoneNumber: String = "+251911223344",
    cooldownSeconds: Int = 45,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onVerifyOtp: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    var otpCode by remember { mutableStateOf("") }
    val isValid = otpCode.length == 6

    val heroGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF02173A),
            Color(0xFF072C67),
            Color(0xFF0E4A9E),
            Color(0xFF1B64CE)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02173A))
    ) {
        // ─── TOP HERO BLUE HEADER ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .background(heroGradient)
                .statusBarsPadding()
                .padding(28.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                // SLA Security pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vision SLA Security",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Verification\nCode",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 42.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter the 6-digit authentication code sent to $phoneNumber",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // ─── WHITE CURVED BODY CONTAINER ──────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.25f),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Enter 6-Digit Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101828)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Code expires in 5 minutes. Do not share your code.",
                        fontSize = 13.sp,
                        color = Color(0xFF667085)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 6 OTP Input Boxes
                    BasicTextField(
                        value = otpCode,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                otpCode = input
                                if (input.length == 6) {
                                    onVerifyOtp(input)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        decorationBox = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (i in 0 until 6) {
                                    val char = otpCode.getOrNull(i)?.toString() ?: ""
                                    val isFocused = otpCode.length == i

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isFocused) Color(0xFFF0F6FE) else Color(0xFFF8FAFC))
                                            .border(
                                                width = if (isFocused) 2.dp else 1.dp,
                                                color = if (isFocused) Color(0xFF1B64CE) else Color(0xFFE4E7EC),
                                                shape = RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color(0xFF101828),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            color = Color(0xFFD92D20),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Resend countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (cooldownSeconds > 0) {
                            Text(
                                text = "Resend code in ${cooldownSeconds}s",
                                fontSize = 13.sp,
                                color = Color(0xFF667085)
                            )
                        } else {
                            Text(
                                text = "Didn't receive code? Resend",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B64CE),
                                modifier = Modifier.clickable(onClick = onResendOtp)
                            )
                        }
                    }
                }

                // Verify & Continue Button
                Button(
                    onClick = { onVerifyOtp(otpCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF101828),
                        disabledContainerColor = Color(0xFFE4E7EC)
                    ),
                    enabled = isValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Verify & Proceed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isValid) Color.White else Color(0xFF98A2B3)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isValid) Color.White else Color(0xFF98A2B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
