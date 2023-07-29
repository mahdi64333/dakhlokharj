package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.os.FileObserver
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
    private val archiveDir = File(filesDir, "archive").also {
        if (!it.exists()) it.mkdir()
    }
    private val _currentDbFileStateFlow = MutableStateFlow<File>(dataRepository.dbFile)
    private val currentDbFileStateFlow get() = _currentDbFileStateFlow.asStateFlow()
    val currentDbFile get() = currentDbFileStateFlow.value
    private val _dbArchiveFileStateFlow =
        MutableStateFlow<List<File>>(archiveDir.listFiles()?.toList() ?: emptyList())

    @Suppress("DEPRECATION")
    private val dbArchiveFileObserver = object : FileObserver(archiveDir.absolutePath) {
        override fun onEvent(event: Int, path: String?) {
            _dbArchiveFileStateFlow.update {
                archiveDir.listFiles()?.toList() ?: emptyList()
            }
        }
    }

    @Suppress("DEPRECATION")
    private val currentDbFileObserver = object : FileObserver(currentDbFile.absolutePath) {
        override fun onEvent(event: Int, path: String?) {
            _currentDbFileStateFlow.update {
                currentDbFile
            }
        }
    }
    val currentDbAliasStateFlow by lazy {
        runBlocking {
            settingsDataStore.getCurrentDbAliasFlow().stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                settingsDataStore.getCurrentDbAliasFlow().first()
            )
        }
    }
    val currentDbAlias get() = currentDbAliasStateFlow.value
    private val _allDbFilesStateFlow = MutableStateFlow<List<File>>(emptyList())
    val allDbFilesStateFlow get() = _allDbFilesStateFlow.asStateFlow()

    init {
        currentDbFileObserver.startWatching()
        dbArchiveFileObserver.startWatching()
        _dbArchiveFileStateFlow.combine(currentDbFileStateFlow) { dbArchiveFiles, currentDbFile ->
            _allDbFilesStateFlow.update {
                dbArchiveFiles.plus(currentDbFile).sortedByDescending { it.lastModified() }
            }
        }.launchIn(viewModelScope)
    }

    fun newDatabaseArchive(newDatabaseArchiveAlias: String) {
        // Saving the previous database archive
        saveDb(dataRepository.dbFile, archiveDir)

        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.setCurrentDbAlias(newDatabaseArchiveAlias)
            dataRepository.clearAllTables()
        }
    }

    private fun saveDb(dbFile: File, archiveDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val previousDbAlias = currentDbAlias.ifEmpty { "default" }

            val savedArchivesNames = _dbArchiveFileStateFlow.value.map {
                it.nameWithoutExtension
            }

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