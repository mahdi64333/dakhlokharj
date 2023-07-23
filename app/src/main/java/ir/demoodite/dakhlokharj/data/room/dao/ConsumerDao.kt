package ir.demoodite.dakhlokharj.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumedProductId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumerResidentId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumerDao {
    @Query(
        "SELECT $residentName FROM $purchasesTableName " +
                "LEFT JOIN $consumersTableName ON $purchaseId = $consumedProductId " +
                "LEFT JOIN $residentsTableName ON $consumerResidentId = $residentId " +
                "WHERE $purchaseId = :purchaseId"
    )
    fun getConsumerNamesOfPurchase(purchaseId: Long): Flow<List<String>>

    @Query(
        "SELECT * FROM $consumersTableName " +
                "WHERE $consumedProductId = :purchaseId"
    )
    fun getConsumersOfPurchase(purchaseId: Long): Flow<List<Consumer>>

    @Insert
    suspend fun insert(consumers: List<Consumer>)
}