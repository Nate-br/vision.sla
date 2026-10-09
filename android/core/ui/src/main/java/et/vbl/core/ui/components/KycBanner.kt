package et.vbl.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import et.vbl.core.designsystem.theme.VblTheme

@Composable
fun KycBanner(
    kycStatus: String = "NOT_SUBMITTED",
    onVerifyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (kycStatus == "APPROVED") return

    val isPending = kycStatus == "PENDING"
    val isRejected = kycStatus == "REJECTED"

    val bgColor = when {
        isPending -> Color(0xFFFFF8E1)
        isRejected -> Color(0xFFFFEBEE)
        else -> Color(0xFFE3F2FD)
    }

    val textColor = when {
        isPending -> Color(0xFFF57F17)
        isRejected -> Color(0xFFC62828)
        else -> Color(0xFF1565C0)
    }

    val title = when {
        isPending -> "Identity Verification In Progress"
        isRejected -> "Identity Verification Required (Action Needed)"
        else -> "Verify Your Identity"
    }

    val description = when {
        isPending -> "Your ID documents are currently under review by the VBL SLA compliance team."
        isRejected -> "Your document was rejected. Please re-upload a clear copy of your National ID or Fayda."
        else -> "Complete KYC to unlock full savings limits and apply for low-interest loans."
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = !isPending, role = Role.Button, onClick = onVerifyClick)
            .padding(16.dp)
            .semantics { role = Role.Button },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isPending) {
            Text(
                text = "Verify →",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Preview(name = "KycBanner - Not Submitted", showBackground = true)
@Composable
private fun KycBannerNotSubmittedPreview() {
    VblTheme(darkTheme = false) {
        KycBanner(kycStatus = "NOT_SUBMITTED")
    }
}

@Preview(name = "KycBanner - Pending", showBackground = true)
@Composable
private fun KycBannerPendingPreview() {
    VblTheme(darkTheme = false) {
        KycBanner(kycStatus = "PENDING")
    }
}
