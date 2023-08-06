package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchasePrice
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseProduct
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchaseTime
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentId

@Entity(
    tableName = purchasesTableName,
    foreignKeys = [ForeignKey(
        entity = Resident::class,
        parentColumns = [residentId],
        childColumns = [purchaseBuyerId],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = purchaseId) val id: Long = 0,
    @ColumnInfo(name = purchaseProduct) var product: String = "",
    @ColumnInfo(name = purchasePrice) var price: Long = 0,
    @ColumnInfo(name = purchaseBuyerId, index = true) var buyerId: Long = 0,
    @ColumnInfo(name = purchaseTime) var time: Long = 0,
)
