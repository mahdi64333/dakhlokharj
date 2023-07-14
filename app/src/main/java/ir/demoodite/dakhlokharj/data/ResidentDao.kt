package ir.demoodite.dakhlokharj.data

import androidx.room.*
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumedProductId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumerResidentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasePrice
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseTime
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.models.database.ResidentSummery
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

    @Query(
        "WITH credits AS " +
                "(" +
                "   SELECT $residentId, SUM($purchasePrice) AS credit" +
                "   FROM $residentsTableName" +
                "   LEFT JOIN $purchasesTableName " +
                "   ON $residentsTableName.$residentId = $purchasesTableName.$purchaseBuyerId " +
                "   GROUP BY $residentId" +
                "), " +
        "debts AS " +
                "(" +
                "   SELECT $residentId, SUM(splitPrice) AS debt " +
                "   FROM $residentsTableName " +
                "   LEFT JOIN $consumersTableName " +
                "   ON $residentsTableName.$residentId = $consumersTableName.$consumerResidentId " +
                "   LEFT JOIN " +
                "   (" +
                "       SELECT $purchaseId, " +
                "           $purchasePrice / COUNT($consumerResidentId) AS splitPrice " +
                "       FROM $purchasesTableName " +
                "           LEFT JOIN $consumersTableName " +
                "           ON $purchaseId = $consumedProductId " +
                "       GROUP BY $purchaseId" +
                "   ) AS splitPrices " +
                "   ON $consumersTableName.$consumedProductId = splitPrices.$purchaseId " +
                "   GROUP BY $residentId" +
                ") " +
        " SELECT $residentsTableName.$residentName, credits.credit AS credit, debts.debt AS debt " +
                "FROM $residentsTableName, credits, debts " +
                "WHERE $residentActive = 1 " +
                "AND credits.$residentId = $residentsTableName.$residentId " +
                "AND debts.$residentId = $residentsTableName.$residentId"
    )
    fun getAllSummaries(): Flow<List<ResidentSummery>>

    @Query(
        "WITH credits AS " +
                "(" +
                "   SELECT $residentId, SUM($purchasePrice) AS credit" +
                "   FROM $residentsTableName" +
                "   LEFT JOIN $purchasesTableName " +
                "   ON $residentsTableName.$residentId = $purchasesTableName.$purchaseBuyerId " +
                "   WHERE $purchasesTableName.$purchaseTime BETWEEN :startTime AND :endTime " +
                "   GROUP BY $residentId" +
                "), " +
                "debts AS " +
                "(" +
                "   SELECT $residentId, SUM(splitPrice) AS debt " +
                "   FROM $residentsTableName " +
                "   LEFT JOIN $consumersTableName " +
                "   ON $residentsTableName.$residentId = $consumersTableName.$consumerResidentId " +
                "   LEFT JOIN " +
                "   (" +
                "       SELECT $purchaseId, " +
                "           $purchasePrice / COUNT($consumerResidentId) AS splitPrice " +
                "       FROM $purchasesTableName " +
                "           LEFT JOIN $consumersTableName " +
                "           ON $purchaseId = $consumedProductId " +
                "       WHERE $purchasesTableName.$purchaseTime BETWEEN :startTime AND :endTime " +
                "       GROUP BY $purchaseId" +
                "   ) AS splitPrices " +
                "   ON $consumersTableName.$consumedProductId = splitPrices.$purchaseId " +
                "   GROUP BY $residentId" +
                ") " +
                " SELECT $residentsTableName.$residentName, credits.credit AS credit, debts.debt AS debt " +
                "FROM $residentsTableName, credits, debts " +
                "WHERE $residentActive = 1 " +
                "AND credits.$residentId = $residentsTableName.$residentId " +
                "AND debts.$residentId = $residentsTableName.$residentId"
    )
    fun getAllSummariesBetween(startTime: Long, endTime: Long): Flow<List<ResidentSummery>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(resident: Resident): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(resident: Resident): Int

    @Delete
    suspend fun delete(resident: Resident)
}