package et.vbl.feature.payments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.model.PaymentProvider

@Composable
fun BankLogoBadge(
    provider: PaymentProvider,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (provider) {
            PaymentProvider.TELEBIRR -> {
                // Telebirr: Electric Cyan Blue + Brand Yellow Accent
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF0091DA)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.7f)) {
                        val w = this.size.width
                        val h = this.size.height
                        // White lowercase "t" stem
                        drawLine(
                            color = Color.White,
                            start = Offset(w * 0.38f, h * 0.22f),
                            end = Offset(w * 0.38f, h * 0.78f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // "t" crossbar
                        drawLine(
                            color = Color.White,
                            start = Offset(w * 0.22f, h * 0.40f),
                            end = Offset(w * 0.58f, h * 0.40f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Warm yellow Telebirr smile arc
                        drawArc(
                            color = Color(0xFFFFB81C),
                            startAngle = 10f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(w * 0.35f, h * 0.42f),
                            size = Size(w * 0.45f, h * 0.40f),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            PaymentProvider.CBE, PaymentProvider.CBE_BIRR -> {
                // Commercial Bank of Ethiopia: Iconic Royal Purple + Bright Gold
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF5B1675)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.68f)) {
                        val w = this.size.width
                        val h = this.size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // Golden diamond/flower CBE emblem
                        val path = Path().apply {
                            moveTo(cx, h * 0.12f)
                            lineTo(w * 0.88f, cy)
                            lineTo(cx, h * 0.88f)
                            lineTo(w * 0.12f, cy)
                            close()
                        }
                        drawPath(path, color = Color(0xFFFFC20E))

                        // Inner purple center
                        drawCircle(
                            color = Color(0xFF5B1675),
                            radius = w * 0.16f,
                            center = Offset(cx, cy)
                        )
                        // Core golden dot
                        drawCircle(
                            color = Color.White,
                            radius = w * 0.08f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            PaymentProvider.BANK_OF_ABYSSINIA -> {
                // Bank of Abyssinia: Midnight Blue + Heritage Gold
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF002855)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.65f)) {
                        val w = this.size.width
                        val h = this.size.height
                        // Stylized golden "A" chevron
                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.15f)
                            lineTo(w * 0.82f, h * 0.82f)
                            lineTo(w * 0.68f, h * 0.82f)
                            lineTo(w * 0.5f, h * 0.42f)
                            lineTo(w * 0.32f, h * 0.82f)
                            lineTo(w * 0.18f, h * 0.82f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFD4AF37))
                        // Gold crossbar
                        drawLine(
                            color = Color(0xFFD4AF37),
                            start = Offset(w * 0.32f, h * 0.65f),
                            end = Offset(w * 0.68f, h * 0.65f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            PaymentProvider.AWASH -> {
                // Awash Bank: Deep Blue + Vibrant Orange Chevron
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF003366)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.65f)) {
                        val w = this.size.width
                        val h = this.size.height
                        val path = Path().apply {
                            moveTo(w * 0.15f, h * 0.3f)
                            lineTo(w * 0.5f, h * 0.72f)
                            lineTo(w * 0.85f, h * 0.3f)
                            lineTo(w * 0.68f, h * 0.3f)
                            lineTo(w * 0.5f, h * 0.52f)
                            lineTo(w * 0.32f, h * 0.3f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFF37021))
                    }
                }
            }

            PaymentProvider.DASHEN -> {
                // Dashen Bank: Navy Blue + Golden Sunburst
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF0A1C3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.65f)) {
                        val cx = this.size.width / 2f
                        val cy = this.size.height / 2f
                        drawCircle(color = Color(0xFFEAAA00), radius = cx * 0.55f, center = Offset(cx, cy))
                        drawCircle(color = Color(0xFF0A1C3C), radius = cx * 0.30f, center = Offset(cx, cy))
                    }
                }
            }

            PaymentProvider.MPESA -> {
                // Safaricom M-Pesa: Clean Green + Red Arrow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF00853F)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.65f)) {
                        val w = this.size.width
                        val h = this.size.height
                        // Red ribbon accent
                        drawLine(
                            color = Color(0xFFE30613),
                            start = Offset(w * 0.2f, h * 0.3f),
                            end = Offset(w * 0.8f, h * 0.7f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(w * 0.8f, h * 0.3f),
                            end = Offset(w * 0.2f, h * 0.7f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            PaymentProvider.COOPAY_EBIRR -> {
                // Cooperative Bank of Oromia: Forest Green + Bright Orange
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF00823B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COOP",
                        color = Color(0xFFF68B1F),
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.24f).sp
                    )
                }
            }

            PaymentProvider.SIINQEE -> {
                // Siinqee Bank: Emerald Green + Gold
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF0C5C36)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SQ",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.32f).sp
                    )
                }
            }

            PaymentProvider.KAAFI_EBIRR -> {
                // Kaafi: Deep Blue + Yellow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF1E3A8A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KF",
                        color = Color(0xFFFACC15),
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.32f).sp
                    )
                }
            }

            PaymentProvider.ZEMEN -> {
                // Zemen: Crimson Maroon
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF7F1D1D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Z",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (size.value * 0.40f).sp
                    )
                }
            }
        }
    }
}
