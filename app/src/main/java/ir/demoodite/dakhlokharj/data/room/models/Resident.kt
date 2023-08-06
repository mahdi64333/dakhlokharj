package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentActive
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentId
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentName
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.residentsTableName

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
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = residentId) val id: Long = 0,
    @ColumnInfo(name = residentName) var name: String = "",
    @ColumnInfo(name = residentActive) var active: Boolean = true,
)
