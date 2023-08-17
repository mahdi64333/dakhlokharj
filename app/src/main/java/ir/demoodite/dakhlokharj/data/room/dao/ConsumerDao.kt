package ir.demoodite.dakhlokharj.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_PRODUCT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASES_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENTS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ACTIVE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_DELETED
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_NAME
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumerDao {
    @Query(
        "SELECT * FROM $CONSUMERS_TABLE_NAME"
    )
    fun getAll(): Flow<List<Consumer>>

    @Query(
        "SELECT $RESIDENT_ID, $RESIDENT_NAME, $RESIDENT_ACTIVE, $RESIDENT_DELETED " +
                "FROM $PURCHASES_TABLE_NAME " +
                "LEFT JOIN $CONSUMERS_TABLE_NAME ON $PURCHASE_ID = $CONSUMERS_PRODUCT_ID " +
                "LEFT JOIN $RESIDENTS_TABLE_NAME ON $CONSUMERS_RESIDENT_ID = $RESIDENT_ID " +
                "WHERE $PURCHASE_ID = :purchaseId"
    )
    fun getConsumerResidentsOfPurchase(purchaseId: Long): Flow<List<Resident>>

    @Query(
        "SELECT * FROM $CONSUMERS_TABLE_NAME " +
                "WHERE $CONSUMERS_PRODUCT_ID = :purchaseId"
    )
    fun getConsumersOfPurchase(purchaseId: Long): Flow<List<Consumer>>

    @Insert
    suspend fun insert(consumers: List<Consumer>)
}