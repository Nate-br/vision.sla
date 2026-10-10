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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinSetupScreen(
    onPinCreated: (String) -> Unit = {}
) {
    var initialPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (isConfirming) confirmPin else initialPin

    fun handleDigitPress(digit: String) {
        errorMessage = null
        if (!isConfirming) {
            if (initialPin.length < 4) {
                initialPin += digit
                if (initialPin.length == 4) {
                    isConfirming = true
                }
            }
        } else {
            if (confirmPin.length < 4) {
                confirmPin += digit
                if (confirmPin.length == 4) {
                    if (confirmPin == initialPin) {
                        onPinCreated(confirmPin)
                    } else {
                        errorMessage = "PINs do not match. Please try again."
                        initialPin = ""
                        confirmPin = ""
                        isConfirming = false
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        errorMessage = null
        if (isConfirming) {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isConfirming = false
            }
        } else {
            if (initialPin.isNotEmpty()) {
                initialPin = initialPin.dropLast(1)
            }
        }
    }

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
                .weight(0.95f)
                .background(heroGradient)
                .statusBarsPadding()
                .padding(28.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
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
                    text = if (isConfirming) "Confirm\nPIN" else "Create\nSecure PIN",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 42.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isConfirming)
                        "Re-enter the same 4 digits to confirm your security PIN"
                    else
                        "Set up a 4-digit code to authorize savings deposits and loan approvals",
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
                .weight(1.35f),
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = currentInput.length > i
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) Color(0xFF1B64CE) else Color(0xFFF1F3F7))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isFilled) Color(0xFF1B64CE) else Color(0xFFD0D5DD),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = errorMessage!!,
                            fontSize = 13.sp,
                            color = Color(0xFFD92D20),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Numeric Keypad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "⌫")
                    )

                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(if (key.isNotEmpty()) Color(0xFFF4F6F9) else Color.Transparent)
                                        .clickable(enabled = key.isNotEmpty()) {
                                            if (key == "⌫") handleBackspace()
                                            else handleDigitPress(key)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key.isNotEmpty()) {
                                        Text(
                                            text = key,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            color = Color(0xFF101828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
