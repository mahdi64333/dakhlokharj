package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumerResidentId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.consumedProductId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentId

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
