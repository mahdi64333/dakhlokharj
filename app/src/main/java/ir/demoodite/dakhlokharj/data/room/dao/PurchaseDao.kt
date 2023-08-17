package ir.demoodite.dakhlokharj.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_PRODUCT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASES_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_BUYER_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRICE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRODUCT
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_TIME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENTS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ACTIVE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_NAME
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query(
        "SELECT * FROM $PURCHASES_TABLE_NAME"
    )
    fun getAll(): Flow<List<Purchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "ORDER BY $PURCHASE_TIME ASC"
    )
    fun getAllDetailedPurchasesTimeAsc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesTimeDesc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "ORDER BY $PURCHASE_PRICE ASC"
    )
    fun getAllDetailedPurchasesPriceAsc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "ORDER BY $PURCHASE_PRICE DESC"
    )
    fun getAllDetailedPurchasesPriceDesc(): Flow<List<DetailedPurchase>>

    fun getAllDetailedPurchases(orderColumn: String, order: String): Flow<List<DetailedPurchase>> {
        return when {
            orderColumn == PURCHASE_TIME && order == "ASC" -> getAllDetailedPurchasesTimeAsc()
            orderColumn == PURCHASE_TIME && order == "DESC" -> getAllDetailedPurchasesTimeDesc()
            orderColumn == PURCHASE_PRICE && order == "ASC" -> getAllDetailedPurchasesPriceAsc()
            orderColumn == PURCHASE_PRICE && order == "DESC" -> getAllDetailedPurchasesPriceDesc()
            else -> getAllDetailedPurchasesTimeDesc()
        }
    }

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $PURCHASE_PRODUCT LIKE '%' || :productName || '%' " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesByProductName(productName: String): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $PURCHASE_PRICE BETWEEN :minPrice AND :maxPrice " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesByPrice(
        minPrice: Double,
        maxPrice: Double,
    ): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "INNER JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $RESIDENT_ACTIVE = 1 " +
                "AND $PURCHASE_TIME BETWEEN :startTime AND :endTime " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesByTime(startTime: Long, endTime: Long): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $PURCHASES_TABLE_NAME " +
                "LEFT JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $PURCHASE_BUYER_ID = :buyerId " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesByBuyer(buyerId: Long): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $PURCHASE_ID, $PURCHASE_PRODUCT, $PURCHASE_PRICE, " +
                "$PURCHASE_TIME, $PURCHASE_BUYER_ID, $RESIDENT_NAME as buyerName " +
                "FROM $CONSUMERS_TABLE_NAME " +
                "LEFT JOIN $PURCHASES_TABLE_NAME " +
                "ON $CONSUMERS_PRODUCT_ID = $PURCHASE_ID " +
                "LEFT JOIN $RESIDENTS_TABLE_NAME " +
                "ON $PURCHASE_BUYER_ID = $RESIDENT_ID " +
                "WHERE $CONSUMERS_RESIDENT_ID = :consumerId " +
                "ORDER BY $PURCHASE_TIME DESC"
    )
    fun getAllDetailedPurchasesByConsumer(consumerId: Long): Flow<List<DetailedPurchase>>

    @Insert
    suspend fun insert(purchase: Purchase): Long

    @Insert
    suspend fun insert(purchases: List<Purchase>)

    @Delete
    suspend fun delete(purchase: Purchase)

    @Delete
    suspend fun delete(purchases: List<Purchase>)
}