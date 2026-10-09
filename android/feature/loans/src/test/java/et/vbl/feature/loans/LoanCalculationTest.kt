package et.vbl.feature.loans

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoanCalculationTest {

    private fun calculateInstallment(principal: Double, annualRatePercent: Double, tenureMonths: Int): Double {
        val totalInterest = principal * (annualRatePercent / 100.0) * (tenureMonths / 12.0)
        val totalDue = principal + totalInterest
        return totalDue / tenureMonths
    }

    @Test
    fun `calculates 12 month flat rate installment accurately`() {
        // 12,000 ETB at 12% p.a. for 12 months = 1,440 interest, 13,440 total due, 1,120 per month
        val monthly = calculateInstallment(12000.0, 12.0, 12)
        assertEquals(1120.0, monthly, 0.001)
    }

    @Test
    fun `calculates 6 month short term loan installment accurately`() {
        // 10,000 ETB at 10% p.a. for 6 months = 500 interest, 10,500 total due, 1,750 per month
        val monthly = calculateInstallment(10000.0, 10.0, 6)
        assertEquals(1750.0, monthly, 0.001)
    }
}
