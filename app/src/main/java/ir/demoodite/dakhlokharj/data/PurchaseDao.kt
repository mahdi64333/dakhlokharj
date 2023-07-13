package ir.demoodite.dakhlokharj.data

import androidx.room.Dao
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.models.database.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query(
        "SELECT * FROM $purchasesTableName " +
                "INNER JOIN $residentsTableName " +
                "ON $purchaseBuyerId = $residentId"
    )
    fun getAllDetailedPurchases(): Flow<List<Purchase>>
}