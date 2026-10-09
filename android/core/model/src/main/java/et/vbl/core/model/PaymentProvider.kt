package et.vbl.core.model

/**
 * Payment provider enum — matches verify.et `bank` field values.
 *
 * All providers listed in the plan Section 4.1.
 * Zemen is listed as NOT_SUPPORTED (manual review only).
 */
enum class PaymentProvider(
    /** The `bank` field value sent to verify.et */
    val verifyEtBank: String?,
    val displayName: String,
    val isSupported: Boolean,
) {
    CBE(
        verifyEtBank = "cbe",
        displayName  = "Commercial Bank of Ethiopia",
        isSupported  = true,
    ),
    TELEBIRR(
        verifyEtBank = "telebirr",
        displayName  = "Telebirr",
        isSupported  = true,
    ),
    CBE_BIRR(
        verifyEtBank = "cbebirr",
        displayName  = "CBE Birr",
        isSupported  = true,
    ),
    BANK_OF_ABYSSINIA(
        verifyEtBank = "boa",
        displayName  = "Bank of Abyssinia",
        isSupported  = true,
    ),
    MPESA(
        verifyEtBank = "mpesa",
        displayName  = "M-Pesa",
        isSupported  = true,
    ),
    DASHEN(
        verifyEtBank = "dashen",
        displayName  = "Dashen Bank",
        isSupported  = true,
    ),
    AWASH(
        verifyEtBank = "awash",
        displayName  = "Awash Bank",
        isSupported  = true,
    ),
    SIINQEE(
        verifyEtBank = "siinqee",
        displayName  = "Siinqee Bank",
        isSupported  = true,
    ),
    KAAFI_EBIRR(
        verifyEtBank = "kaafiebirr",
        displayName  = "Kaafi Ebirr",
        isSupported  = true,
    ),
    COOPAY_EBIRR(
        verifyEtBank = "coopayebirr",
        displayName  = "Coopay Ebirr",
        isSupported  = true,
    ),
    /** Not supported by verify.et — always goes to manual review */
    ZEMEN(
        verifyEtBank = null,
        displayName  = "Zemen Bank",
        isSupported  = false,
    ),
}
