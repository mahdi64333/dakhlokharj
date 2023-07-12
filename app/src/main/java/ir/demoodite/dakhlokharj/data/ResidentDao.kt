package ir.demoodite.dakhlokharj.data

import androidx.room.*
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
    @Query(
        "SELECT * FROM $residentsTableName"
    )
    fun getAll(): Flow<List<Resident>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(resident: Resident): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(resident: Resident): Int
}