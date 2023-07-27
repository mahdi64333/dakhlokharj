package ir.demoodite.dakhlokharj.ui.components.databasemanager

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DatabaseManagerViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    fun archiveCurrentDatabase(filesDir: File, alias: String) {
        val archiveDir = File(filesDir, "archive")
        if (!archiveDir.exists()) {
            archiveDir.mkdir()
        }

        var archivedDbFile = File(archiveDir, "${alias}.db")
        var index = 1
        while (archivedDbFile.exists()) {
            index++
            archivedDbFile = File(archiveDir, "${alias}_${index}.db")
        }
        archivedDbFile.writeBytes(dataRepository.dbFile.readBytes())
    }
}