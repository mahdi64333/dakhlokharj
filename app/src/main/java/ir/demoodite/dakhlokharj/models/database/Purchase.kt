package ir.demoodite.dakhlokharj.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseBuyerId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasePrice
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseProduct
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseTime
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasesTableName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId

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
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = purchaseId, index = true) val id: Long,
    @ColumnInfo(name = purchaseProduct) var product: String,
    @ColumnInfo(name = purchasePrice) var price: Long,
    @ColumnInfo(name = purchaseBuyerId, index = true) var buyerId: Long,
    @ColumnInfo(name = purchaseTime) var time: Long,
)
