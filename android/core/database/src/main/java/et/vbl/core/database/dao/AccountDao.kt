package et.vbl.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import et.vbl.core.database.model.SavingsAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM savings_account LIMIT 1")
    fun getSavingsAccount(): Flow<SavingsAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: SavingsAccountEntity)

    @Query("DELETE FROM savings_account")
    suspend fun clear()
}
