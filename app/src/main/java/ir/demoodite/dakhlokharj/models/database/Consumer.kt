package ir.demoodite.dakhlokharj.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.consumersTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseProductId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId

@Entity(
    tableName = consumersTableName,
    primaryKeys = [
        purchaseProductId,
        consumerId,
    ],
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = [purchaseId],
            childColumns = [purchaseProductId],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Resident::class,
            parentColumns = [residentId],
            childColumns = [consumerId],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
)
data class Consumer(
    @ColumnInfo(name = purchaseProductId) val product: Long,
    @ColumnInfo(name = consumerId, index = true) val consumer: Long,
)
