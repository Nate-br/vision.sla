package et.vbl.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import et.vbl.core.designsystem.theme.VblTheme

@Composable
fun PhoneInputScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onContinue: (String) -> Unit = {}
) {
    var rawPhone by remember { mutableStateOf("") }
    val isValid = rawPhone.length == 9 && (rawPhone.startsWith("9") || rawPhone.startsWith("7"))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Welcome to Vision for a Better Life",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your phone number to continue. We will send you a 6-digit verification code.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "+251",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .weight(0.3f)
                            .padding(end = 8.dp),
                        label = { Text("Code") }
                    )
                    OutlinedTextField(
                        value = rawPhone,
                        onValueChange = { input ->
                            if (input.length <= 9 && input.all { it.isDigit() }) {
                                rawPhone = input
                            }
                        },
                        modifier = Modifier.weight(0.7f),
                        label = { Text("Phone Number") },
                        placeholder = { Text("911223344") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = errorMessage != null || (rawPhone.isNotEmpty() && !isValid),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = { onContinue("+251$rawPhone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(
                        text = "Send Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(name = "PhoneInput - Light", showBackground = true)
@Composable
private fun PhoneInputScreenLightPreview() {
    VblTheme(darkTheme = false) {
        PhoneInputScreen()
    }
}

@Preview(name = "PhoneInput - Dark", showBackground = true)
@Composable
private fun PhoneInputScreenDarkPreview() {
    VblTheme(darkTheme = true) {
        PhoneInputScreen()
    }
}
