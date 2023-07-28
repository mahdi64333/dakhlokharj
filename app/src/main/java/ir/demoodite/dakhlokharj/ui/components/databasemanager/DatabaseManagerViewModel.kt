package ir.demoodite.dakhlokharj.ui.components.databasemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DatabaseManagerViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val settingsDataStore: SettingsDataStore,
    @Named(AppModule.FILES_DIR_PROVIDER) private val filesDir: File,
) : ViewModel() {
    val currentDbAliasStateFlow by lazy {
        runBlocking {
            settingsDataStore.getCurrentDbAliasFlow().stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                settingsDataStore.getCurrentDbAliasFlow().first()
            )
        }
    }
    private val currentDbAlias get() = currentDbAliasStateFlow.value

    fun newDatabaseArchive(newDatabaseArchiveAlias: String) {
        val archiveDir = File(filesDir, "archive")
        if (!archiveDir.exists()) {
            archiveDir.mkdir()
        }

        // Saving the previous database archive
        saveDb(dataRepository.dbFile, archiveDir)

        viewModelScope.launch {
            settingsDataStore.setCurrentDbAlias(newDatabaseArchiveAlias)
            dataRepository.clearAllTables()
        }
    }

    private fun saveDb(dbFile: File, archiveDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val previousDbAlias = currentDbAlias.ifEmpty { "default" }

            val savedArchivesNames = archiveDir.listFiles()?.map {
                it.nameWithoutExtension
            } ?: listOf()

            val possibleNamesRegex = """${previousDbAlias}(_\d+)?\$""".toRegex()
            val takenFileNames = savedArchivesNames.filter {
                possibleNamesRegex matches it
            }.sorted()

            val dbFileName = if (takenFileNames.isEmpty()) {
                previousDbAlias
            } else {
                val suffixNumberRegex = """(?<=_)\d+\$""".toRegex()
                val suffixNumber =
                    suffixNumberRegex.find(takenFileNames.last())?.value?.toIntOrNull() ?: 0
                "${previousDbAlias}_${suffixNumber}"
            }

            val archivedDbFile = File(archiveDir, "${dbFileName}.db")
            archivedDbFile.writeBytes(dbFile.readBytes())
        }
    }
}