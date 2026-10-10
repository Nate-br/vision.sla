package et.vbl.feature.loans

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.vbl.core.designsystem.theme.VblColors

@Composable
fun LoanApplicationScreen(
    productName: String = "Business Micro Loan",
    initialAmountEtb: Double = 25000.0,
    initialTenureMonths: Int = 12,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmitApplication: (amountEtb: Double, tenureMonths: Int, purpose: String) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var amountText by remember { mutableStateOf(initialAmountEtb.toInt().toString()) }
    var tenureText by remember { mutableStateOf(initialTenureMonths.toString()) }
    var purpose by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val tenure = tenureText.toIntOrNull() ?: 0
    val isValid = amount > 0 && tenure > 0 && purpose.trim().length >= 10 && agreeToTerms

    val heroGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF031634),
            Color(0xFF072C67),
            Color(0xFF0E4698),
            Color(0xFF1B63CD)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        // ─── TOP HERO BLUE HEADER (WITH PROMINENT BACK BUTTON) ────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(heroGradient)
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Top App Bar with Circular Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Loan Application",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Vision SLA Cooperative Financing",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Summary Card inside Blue Hero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = productName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Fast Approval",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Applications are evaluated by SLA credit officers within 24 to 48 hours.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // ─── FORM FIELDS CONTAINER ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // Requested Amount
            Text(
                text = "REQUESTED AMOUNT (ETB)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475467)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 25000") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF101828),
                    unfocusedBorderColor = Color(0xFFD0D5DD)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Desired Tenure
            Text(
                text = "DESIRED TENURE (MONTHS)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475467)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tenureText,
                onValueChange = { if (it.all { c -> c.isDigit() }) tenureText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 12") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF101828),
                    unfocusedBorderColor = Color(0xFFD0D5DD)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loan Purpose
            Text(
                text = "LOAN PURPOSE & USE OF FUNDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475467)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text("Describe how the funds will be used (min 10 characters)...") },
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF101828),
                    unfocusedBorderColor = Color(0xFFD0D5DD)
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VblColors.ErrorLight)
                ) {
                    Text(
                        text = errorMessage,
                        color = VblColors.Error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Terms Agreement Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF101828),
                        uncheckedColor = Color(0xFF98A2B3)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "I confirm the accuracy of this information and agree to the cooperative lending bylaws and deduction terms.",
                    fontSize = 12.sp,
                    color = Color(0xFF475467),
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button (Dark Pill)
            Button(
                onClick = {
                    if (isValid && !isLoading) {
                        onSubmitApplication(amount, tenure, purpose)
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF101828),
                    disabledContainerColor = Color(0xFFEAECF0)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit Application",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isValid) Color.White else Color(0xFF98A2B3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel / Go Back Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackClick)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Back to Loan Catalog",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF667085)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
