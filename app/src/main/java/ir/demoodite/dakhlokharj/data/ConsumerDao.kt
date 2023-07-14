package ir.demoodite.dakhlokharj.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumerResidentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumedProductId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName
import ir.demoodite.dakhlokharj.models.database.Consumer
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

    @Insert
    suspend fun insert(consumers: List<Consumer>)
}