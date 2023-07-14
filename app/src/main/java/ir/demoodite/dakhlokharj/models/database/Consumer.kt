package ir.demoodite.dakhlokharj.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumerResidentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumedProductId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId

@Entity(
    tableName = consumersTableName,
    primaryKeys = [
        consumedProductId,
        consumerResidentId,
    ],
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = [purchaseId],
            childColumns = [consumedProductId],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Resident::class,
            parentColumns = [residentId],
            childColumns = [consumerResidentId],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
)
data class Consumer(
    @ColumnInfo(name = consumedProductId) val productId: Long,
    @ColumnInfo(name = consumerResidentId, index = true) val consumerId: Long,
)
