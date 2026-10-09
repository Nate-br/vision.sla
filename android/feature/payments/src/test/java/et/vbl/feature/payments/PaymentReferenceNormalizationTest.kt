package et.vbl.feature.payments

import et.vbl.core.model.PaymentProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentReferenceNormalizationTest {

    private fun normalize(reference: String): String {
        return reference.uppercase().replace(Regex("[\\s-]+"), "")
    }

    @Test
    fun `normalizes Telebirr references by uppercase and removing spaces and dashes`() {
        assertEquals("RA47KL92X", normalize("  ra-47 kl 92 x "))
        assertEquals("RA47KL92X", normalize("ra47kl92x"))
        assertEquals("RA47KL92X", normalize("RA-47-KL-92X"))
    }

    @Test
    fun `normalizes CBE and CBE Birr transaction references`() {
        assertEquals("FT2428198712", normalize("ft-24281-98712"))
        assertEquals("CB890123", normalize(" cb 890 123 "))
    }

    @Test
    fun `verifies all 11 providers are mapped correctly`() {
        val providers = PaymentProvider.entries
        assertEquals(11, providers.size)

        val supported = providers.filter { it.isSupported }
        val manualReview = providers.filter { !it.isSupported }

        assertEquals(10, supported.size)
        assertEquals(1, manualReview.size)
        assertEquals(PaymentProvider.ZEMEN, manualReview.first())
    }
}
