package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_PRODUCT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.CONSUMERS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID

@Entity(
    tableName = CONSUMERS_TABLE_NAME,
    primaryKeys = [
        CONSUMERS_PRODUCT_ID,
        CONSUMERS_RESIDENT_ID,
    ],
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = [PURCHASE_ID],
            childColumns = [CONSUMERS_PRODUCT_ID],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Resident::class,
            parentColumns = [RESIDENT_ID],
            childColumns = [CONSUMERS_RESIDENT_ID],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
)
data class Consumer(
    @ColumnInfo(name = CONSUMERS_PRODUCT_ID) var productId: Long,
    @ColumnInfo(name = CONSUMERS_RESIDENT_ID, index = true) val consumerId: Long,
)
