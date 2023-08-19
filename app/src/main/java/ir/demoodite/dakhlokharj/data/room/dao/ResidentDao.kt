package ir.demoodite.dakhlokharj.data.room.dao

import androidx.room.*
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_PRODUCT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASES_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_BUYER_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRICE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_TIME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENTS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ACTIVE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_DELETED
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_NAME
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.data.room.models.ResidentSummery
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
    @Query(
        "SELECT * FROM $RESIDENTS_TABLE_NAME"
    )
    fun getAll(): Flow<List<Resident>>


    @Query(
        "SELECT * FROM $RESIDENTS_TABLE_NAME " +
                "WHERE $RESIDENT_DELETED = 0"
    )
    fun getAllNonDeleted(): Flow<List<Resident>>

    @Query(
        "SELECT * FROM $RESIDENTS_TABLE_NAME " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $RESIDENT_DELETED = 0"
    )
    fun getAllActive(): Flow<List<Resident>>

    @Query(
        "WITH credits AS " +
                "(" +
                "   SELECT $RESIDENT_ID, SUM($PURCHASE_PRICE) AS credit" +
                "   FROM $RESIDENTS_TABLE_NAME" +
                "   LEFT JOIN $PURCHASES_TABLE_NAME " +
                "   ON $RESIDENTS_TABLE_NAME.$RESIDENT_ID = $PURCHASES_TABLE_NAME.$PURCHASE_BUYER_ID " +
                "   GROUP BY $RESIDENT_ID" +
                "), " +
                "debts AS " +
                "(" +
                "   SELECT $RESIDENT_ID, SUM(splitPrice) AS debt " +
                "   FROM $RESIDENTS_TABLE_NAME " +
                "   LEFT JOIN $CONSUMERS_TABLE_NAME " +
                "   ON $RESIDENTS_TABLE_NAME.$RESIDENT_ID = $CONSUMERS_TABLE_NAME.$CONSUMERS_RESIDENT_ID " +
                "   LEFT JOIN " +
                "   (" +
                "       SELECT $PURCHASE_ID, " +
                "           $PURCHASE_PRICE / COUNT($CONSUMERS_RESIDENT_ID) AS splitPrice " +
                "       FROM $PURCHASES_TABLE_NAME " +
                "           LEFT JOIN $CONSUMERS_TABLE_NAME " +
                "           ON $PURCHASE_ID = $CONSUMERS_PRODUCT_ID " +
                "       GROUP BY $PURCHASE_ID" +
                "   ) AS splitPrices " +
                "   ON $CONSUMERS_TABLE_NAME.$CONSUMERS_PRODUCT_ID = splitPrices.$PURCHASE_ID " +
                "   GROUP BY $RESIDENT_ID" +
                ") " +
                " SELECT $RESIDENTS_TABLE_NAME.$RESIDENT_NAME, credits.credit AS credit, debts.debt AS debt " +
                "FROM $RESIDENTS_TABLE_NAME, credits, debts " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $RESIDENT_DELETED = 0 " +
                "AND credits.$RESIDENT_ID = $RESIDENTS_TABLE_NAME.$RESIDENT_ID " +
                "AND debts.$RESIDENT_ID = $RESIDENTS_TABLE_NAME.$RESIDENT_ID"
    )
    fun getAllSummaries(): Flow<List<ResidentSummery>>

    @Query(
        "WITH credits AS " +
                "(" +
                "   SELECT $RESIDENT_ID, SUM($PURCHASE_PRICE) AS credit" +
                "   FROM $RESIDENTS_TABLE_NAME" +
                "   LEFT JOIN $PURCHASES_TABLE_NAME " +
                "   ON $RESIDENTS_TABLE_NAME.$RESIDENT_ID = $PURCHASES_TABLE_NAME.$PURCHASE_BUYER_ID " +
                "   WHERE $PURCHASES_TABLE_NAME.$PURCHASE_TIME BETWEEN :startTime AND :endTime " +
                "   GROUP BY $RESIDENT_ID" +
                "), " +
                "debts AS " +
                "(" +
                "   SELECT $RESIDENT_ID, SUM(splitPrice) AS debt " +
                "   FROM $RESIDENTS_TABLE_NAME " +
                "   LEFT JOIN $CONSUMERS_TABLE_NAME " +
                "   ON $RESIDENTS_TABLE_NAME.$RESIDENT_ID = $CONSUMERS_TABLE_NAME.$CONSUMERS_RESIDENT_ID " +
                "   LEFT JOIN " +
                "   (" +
                "       SELECT $PURCHASE_ID, " +
                "           $PURCHASE_PRICE / COUNT($CONSUMERS_RESIDENT_ID) AS splitPrice " +
                "       FROM $PURCHASES_TABLE_NAME " +
                "           LEFT JOIN $CONSUMERS_TABLE_NAME " +
                "           ON $PURCHASE_ID = $CONSUMERS_PRODUCT_ID " +
                "       WHERE $PURCHASES_TABLE_NAME.$PURCHASE_TIME BETWEEN :startTime AND :endTime " +
                "       GROUP BY $PURCHASE_ID" +
                "   ) AS splitPrices " +
                "   ON $CONSUMERS_TABLE_NAME.$CONSUMERS_PRODUCT_ID = splitPrices.$PURCHASE_ID " +
                "   GROUP BY $RESIDENT_ID" +
                ") " +
                " SELECT $RESIDENTS_TABLE_NAME.$RESIDENT_NAME, credits.credit AS credit, debts.debt AS debt " +
                "FROM $RESIDENTS_TABLE_NAME, credits, debts " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $RESIDENT_DELETED = 0 " +
                "AND credits.$RESIDENT_ID = $RESIDENTS_TABLE_NAME.$RESIDENT_ID " +
                "AND debts.$RESIDENT_ID = $RESIDENTS_TABLE_NAME.$RESIDENT_ID"
    )
    fun getAllSummariesBetween(startTime: Long, endTime: Long): Flow<List<ResidentSummery>>

    @Query(
        "SELECT EXISTS  (" +
                "SELECT * FROM $RESIDENTS_TABLE_NAME " +
                "WHERE $RESIDENT_DELETED = 0 " +
                "AND $RESIDENT_NAME = :name" +
                ")"
    )
    suspend fun isNameTaken(name: String): Boolean

    @Delete
    suspend fun delete(resident: Resident)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(resident: Resident): Long

    @Insert
    suspend fun insert(residents: List<Resident>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(resident: Resident): Int
}