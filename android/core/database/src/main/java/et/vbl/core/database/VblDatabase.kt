package et.vbl.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import et.vbl.core.database.dao.AccountDao
import et.vbl.core.database.dao.LoanDao
import et.vbl.core.database.dao.NotificationDao
import et.vbl.core.database.dao.TransactionDao
import et.vbl.core.database.model.ActiveLoanEntity
import et.vbl.core.database.model.NotificationEntity
import et.vbl.core.database.model.SavingsAccountEntity
import et.vbl.core.database.model.TransactionEntity

@Database(
    entities = [
        SavingsAccountEntity::class,
        TransactionEntity::class,
        ActiveLoanEntity::class,
        NotificationEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class VblDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun notificationDao(): NotificationDao
}
