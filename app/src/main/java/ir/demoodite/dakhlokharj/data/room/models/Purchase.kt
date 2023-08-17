package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASES_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_BUYER_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRICE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRODUCT
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_TIME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID

@Entity(
    tableName = PURCHASES_TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = Resident::class,
        parentColumns = [RESIDENT_ID],
        childColumns = [PURCHASE_BUYER_ID],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = PURCHASE_ID) val id: Long = 0,
    @ColumnInfo(name = PURCHASE_PRODUCT) var product: String = "",
    @ColumnInfo(name = PURCHASE_PRICE) var price: Double = 0.0,
    @ColumnInfo(name = PURCHASE_BUYER_ID, index = true) var buyerId: Long = 0,
    @ColumnInfo(name = PURCHASE_TIME) var time: Long = 0,
)
