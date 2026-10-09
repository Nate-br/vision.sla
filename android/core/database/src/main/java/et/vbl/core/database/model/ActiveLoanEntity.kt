package et.vbl.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_loans")
data class ActiveLoanEntity(
    @PrimaryKey val id: String,
    val productName: String,
    val loanType: String,
    val totalDueSantim: String,
    val monthlyInstallmentSantim: String,
    val status: String,
    val nextPaymentDueDate: String?,
    val paidInstallments: Int,
    val totalInstallments: Int,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
