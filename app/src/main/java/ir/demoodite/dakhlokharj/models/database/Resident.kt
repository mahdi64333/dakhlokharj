package ir.demoodite.dakhlokharj.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.residentsTableName

@Entity(
    tableName = residentsTableName,
    indices = [
        Index(
            value = [residentName],
            unique = true,
        ),
    ],
)
data class Resident(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = residentId) val id: Long,
    @ColumnInfo(name = residentName) var name: String,
    @ColumnInfo(name = residentActive) var active: Boolean,
)
