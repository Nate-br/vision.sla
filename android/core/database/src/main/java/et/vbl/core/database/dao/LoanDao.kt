package et.vbl.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import et.vbl.core.database.model.ActiveLoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Query("SELECT * FROM active_loans LIMIT 1")
    fun getActiveLoan(): Flow<ActiveLoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(loan: ActiveLoanEntity)

    @Query("DELETE FROM active_loans")
    suspend fun clear()
}
