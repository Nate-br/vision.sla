package et.vbl.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import et.vbl.core.database.VblDatabase
import et.vbl.core.database.dao.AccountDao
import et.vbl.core.database.dao.LoanDao
import et.vbl.core.database.dao.NotificationDao
import et.vbl.core.database.dao.TransactionDao
import et.vbl.core.database.security.DatabasePassphraseManager
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVblDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager
    ): VblDatabase {
        val passphrase = passphraseManager.getOrCreatePassphrase()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            VblDatabase::class.java,
            "vbl_encrypted_database.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAccountDao(database: VblDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: VblDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideLoanDao(database: VblDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideNotificationDao(database: VblDatabase): NotificationDao = database.notificationDao()
}
