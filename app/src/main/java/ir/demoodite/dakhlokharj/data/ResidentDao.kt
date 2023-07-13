package ir.demoodite.dakhlokharj.data

import androidx.room.*
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
    @Query(
        "SELECT * FROM $residentsTableName"
    )
    fun getAll(): Flow<List<Resident>>

    @Query(
        "SELECT * FROM $residentsTableName " +
                "WHERE $residentActive = 1"
    )
    fun getAllActive(): Flow<List<Resident>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(resident: Resident): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(resident: Resident): Int

    @Delete
    suspend fun delete(resident: Resident)
}