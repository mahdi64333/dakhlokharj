package ir.demoodite.dakhlokharj.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.models.database.DetailedPurchase
import ir.demoodite.dakhlokharj.models.database.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query(
        "SELECT *, $residentName as buyerName FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId " +
                "WHERE $residentActive = 1"
    )
    fun getAllDetailedPurchases(): Flow<List<DetailedPurchase>>

    @Insert
    suspend fun insert(purchase: Purchase)

    @Delete
    suspend fun delete(purchase: Purchase)
}