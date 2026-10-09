package et.vbl.core.model

/**
 * Payment attempt state machine — mirrors the backend state machine exactly.
 *
 * Plan Section 4.3:
 *   DRAFT → SUBMITTED → VERIFYING → VERIFIED_CREDITED
 *                         │  ├──► PENDING_RETRY ──► (re-check) ──┐
 *                         │  ├──► MANUAL_REVIEW ──► APPROVED / REJECTED
 *                         └──► REJECTED | EXPIRED
 *
 * App only DISPLAYS these states. The backend is the single source of truth.
 * Never trust a status sent by the app — the server recalculates from its DB.
 */
enum class PaymentStatus(
    /** Plain-language label shown to the member (see strings.xml for localised versions) */
    val displayLabelResKey: String,
    /** Whether money is confirmed safe to communicate to the member */
    val moneyIsSafe: Boolean,
    /** Whether this is a terminal state (no further automatic transitions) */
    val isTerminal: Boolean,
) {
    DRAFT(
        displayLabelResKey = "payment_status_draft",
        moneyIsSafe  = true,
        isTerminal   = false,
    ),
    SUBMITTED(
        displayLabelResKey = "payment_status_submitted",
        moneyIsSafe  = true,
        isTerminal   = false,
    ),
    VERIFYING(
        displayLabelResKey = "payment_status_verifying",
        moneyIsSafe  = true,
        isTerminal   = false,
    ),
    PENDING_RETRY(
        displayLabelResKey = "payment_status_pending_retry",
        moneyIsSafe  = true,
        isTerminal   = false,
    ),
    VERIFIED_CREDITED(
        displayLabelResKey = "payment_status_credited",
        moneyIsSafe  = true,
        isTerminal   = true,
    ),
    MANUAL_REVIEW(
        displayLabelResKey = "payment_status_manual_review",
        moneyIsSafe  = true,
        isTerminal   = false,
    ),
    APPROVED(
        displayLabelResKey = "payment_status_approved",
        moneyIsSafe  = true,
        isTerminal   = true,
    ),
    REJECTED(
        displayLabelResKey = "payment_status_rejected",
        moneyIsSafe  = true,   // always reassure the member their money didn't vanish
        isTerminal   = true,
    ),
    EXPIRED(
        displayLabelResKey = "payment_status_expired",
        moneyIsSafe  = true,
        isTerminal   = true,
    ),
}
