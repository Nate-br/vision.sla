package et.vbl.feature.payments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import et.vbl.core.model.PaymentProvider

@Composable
fun BankLogoBadge(
    provider: PaymentProvider,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val logoRes = when (provider) {
        PaymentProvider.TELEBIRR -> R.drawable.logo_telebirr
        PaymentProvider.CBE -> R.drawable.logo_cbe
        PaymentProvider.CBE_BIRR -> R.drawable.logo_cbe_birr
        PaymentProvider.BANK_OF_ABYSSINIA -> R.drawable.logo_abyssinia
        PaymentProvider.AWASH -> R.drawable.logo_awash
        PaymentProvider.DASHEN -> R.drawable.logo_dashen
        PaymentProvider.MPESA -> R.drawable.logo_mpesa
        PaymentProvider.COOPAY_EBIRR -> R.drawable.logo_coop
        PaymentProvider.SIINQEE -> R.drawable.logo_siinqee
        else -> R.drawable.logo_telebirr
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0F2F5), RoundedCornerShape(14.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = provider.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size * 0.82f)
        )
    }
}
