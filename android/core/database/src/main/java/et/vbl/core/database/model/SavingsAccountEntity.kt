package et.vbl.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_account")
data class SavingsAccountEntity(
    @PrimaryKey val accountNumber: String,
    val totalSavingsSantim: String,
    val availableSantim: String,
    val frozenSantim: String,
    val totalInterestEarnedSantim: String,
    val currency: String,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
