package et.vbl.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val transactionType: String,
    val amountSantim: String,
    val balanceAfterSantim: String,
    val reference: String?,
    val paymentProvider: String?,
    val status: String,
    val description: String?,
    val createdAtIso: String,
    val createdAtTimestamp: Long
)
