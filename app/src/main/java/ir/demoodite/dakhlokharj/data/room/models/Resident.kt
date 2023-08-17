package ir.demoodite.dakhlokharj.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENTS_TABLE_NAME
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ACTIVE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_DELETED
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_ID
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.RESIDENT_NAME

@Entity(
    tableName = RESIDENTS_TABLE_NAME,
)
data class Resident(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = RESIDENT_ID) val id: Long = 0,
    @ColumnInfo(name = RESIDENT_NAME) var name: String = "",
    @ColumnInfo(name = RESIDENT_ACTIVE) var active: Boolean = true,
    @ColumnInfo(name = RESIDENT_DELETED) var deleted: Boolean = false,
)
