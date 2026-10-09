package et.vbl.core.common.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Money utilities for VBL.
 *
 * RULE: Money is stored as integer minor units (santim, 1 ETB = 100 santim).
 * NEVER use Double or Float for money calculations.
 *
 * Amounts on the network are integer minor units (Long).
 * Amounts displayed to the user use BigDecimal scaled to 2 decimal places.
 */
object Money {

    /** Convert minor units (santim) to display BigDecimal. e.g. 150000L → 1500.00 */
    fun fromMinorUnits(santim: Long): BigDecimal =
        BigDecimal(santim).setScale(2).divide(BigDecimal(100), 2, RoundingMode.UNNECESSARY)

    /** Convert BigDecimal display amount to minor units. e.g. 1500.00 → 150000L */
    fun toMinorUnits(etb: BigDecimal): Long =
        etb.setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toLong()

    /**
     * Format for display: "ETB 1,500.00"
     * Per plan Section 10: amounts shown as `ETB 1,500.00` consistently.
     */
    fun format(santim: Long): String {
        val bd = fromMinorUnits(santim)
        val formatted = "%,.2f".format(bd.toDouble())
        return "ETB $formatted"
    }

    /** Format BigDecimal display amount. */
    fun format(etb: BigDecimal): String {
        val formatted = "%,.2f".format(etb.toDouble())
        return "ETB $formatted"
    }
}
