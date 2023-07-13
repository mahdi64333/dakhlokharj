package ir.demoodite.dakhlokharj.data

import androidx.room.Dao
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseProductId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumerDao {
    @Query(
        "SELECT $residentName FROM $purchasesTableName " +
                "LEFT JOIN $consumersTableName ON $purchaseId = $purchaseProductId " +
                "LEFT JOIN $residentsTableName ON $consumerId = $residentId " +
                "WHERE $purchaseId = :purchaseId"
    )
    fun getConsumerNamesOfPurchase(purchaseId: Long): Flow<List<String>>
}