package ir.demoodite.dakhlokharj.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchasePrice
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseProduct
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseTime
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query(
        "SELECT $purchaseId, $purchaseProduct, $purchasePrice, " +
                "$purchaseTime, $purchaseBuyerId, $residentName as buyerName " +
                "FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1 " +
                "ORDER BY $purchaseTime ASC"
    )
    fun getAllDetailedPurchasesTimeAsc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $purchaseId, $purchaseProduct, $purchasePrice, " +
                "$purchaseTime, $purchaseBuyerId, $residentName as buyerName " +
                "FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1 " +
                "ORDER BY $purchaseTime DESC"
    )
    fun getAllDetailedPurchasesTimeDesc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $purchaseId, $purchaseProduct, $purchasePrice, " +
                "$purchaseTime, $purchaseBuyerId, $residentName as buyerName " +
                "FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1 " +
                "ORDER BY $purchasePrice ASC"
    )
    fun getAllDetailedPurchasesPriceAsc(): Flow<List<DetailedPurchase>>

    @Query(
        "SELECT $purchaseId, $purchaseProduct, $purchasePrice, " +
                "$purchaseTime, $purchaseBuyerId, $residentName as buyerName " +
                "FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1 " +
                "ORDER BY $purchasePrice DESC"
    )
    fun getAllDetailedPurchasesPriceDesc(): Flow<List<DetailedPurchase>>

    fun getAllDetailedPurchases(orderColumn: String, order: String): Flow<List<DetailedPurchase>> {
        return when {
            orderColumn == purchaseTime && order == "ASC" -> getAllDetailedPurchasesTimeAsc()
            orderColumn == purchaseTime && order == "DESC" -> getAllDetailedPurchasesTimeDesc()
            orderColumn == purchasePrice && order == "ASC" -> getAllDetailedPurchasesPriceAsc()
            orderColumn == purchasePrice && order == "DESC" -> getAllDetailedPurchasesPriceDesc()
            else -> getAllDetailedPurchasesTimeDesc()
        }
    }

    @Query(
        "SELECT $purchaseId, $purchaseProduct, $purchasePrice, " +
                "$purchaseTime, $purchaseBuyerId, $residentName as buyerName " +
                "FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1 " +
                "AND $purchaseProduct LIKE '%' || :productName || '%' " +
                "ORDER BY $purchaseTime DESC"
    )
    fun getAllDetailedPurchasesByProductName(productName: String): Flow<List<DetailedPurchase>>

    @Insert
    suspend fun insert(purchase: Purchase): Long

    @Delete
    suspend fun delete(purchase: Purchase)
}