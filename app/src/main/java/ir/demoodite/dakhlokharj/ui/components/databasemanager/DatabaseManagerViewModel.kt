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
    @Named(AppModule.CACHE_DIR_PROVIDER) private val cacheDir: File,
    private val databaseImporter: DataRepository.Companion.DatabaseImporter,
) : ViewModel() {
    private val archiveDir = File(filesDir, "archive").also {
        if (!it.exists()) it.mkdir()
    }
    val currentDbFile get() = dataRepository.dbFile
    private val _allDbArchivesStateFlow =
        MutableStateFlow<List<Pair<File, String>>>(emptyList())
    val allDbArchivesStateFlow get() = _allDbArchivesStateFlow.asStateFlow()

    @Suppress("DEPRECATION")
    private val dbArchiveFileObserver = object : FileObserver(archiveDir.absolutePath) {
        override fun onEvent(event: Int, path: String?) {
            updateFilesList()
        }
    }

    @Suppress("DEPRECATION")
    private val currentDbFileObserver = object : FileObserver(currentDbFile.absolutePath) {
        override fun onEvent(event: Int, path: String?) {
            updateFilesList()
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

    init {
        startFileObservers()
        viewModelScope.launch {
            currentDbAliasStateFlow.collectLatest {
                updateFilesList()
            }
        }
    }

    private fun updateFilesList() {
        val archivedFilesWithAlias = archiveDir.listFiles()?.map {
            Pair(it, it.nameWithoutExtension)
        } ?: emptyList()
        val currentDbWithAlias = Pair(dataRepository.dbFile, currentDbAlias)

        _allDbArchivesStateFlow.update {
            listOf(currentDbWithAlias).plus(archivedFilesWithAlias)
                .sortedByDescending { it.first.lastModified() }
        }
    }

    private fun startFileObservers() {
        currentDbFileObserver.startWatching()
        dbArchiveFileObserver.startWatching()
    }

    private fun stopFileObservers() {
        currentDbFileObserver.stopWatching()
        dbArchiveFileObserver.stopWatching()
    }

    fun newDatabaseArchive(newDatabaseArchiveAlias: String) {
        viewModelScope.launch(Dispatchers.IO) {
            archiveCurrentDb()

            settingsDataStore.setCurrentDbAlias(newDatabaseArchiveAlias)
            dataRepository.clearAllTables()
        }
    }

    private fun archiveCurrentDb() {
        val currentDbAlias = currentDbAlias.ifEmpty { "default" }
        val newArchiveFilename = createUniqueArchiveFilename(currentDbAlias)
        val newArchiveFile = File(archiveDir, "${newArchiveFilename}.db")
        newArchiveFile.writeBytes(dataRepository.dbFile.readBytes())
    }

    private fun createUniqueArchiveFilename(alias: String): String {
        val savedArchivesNames = _allDbArchivesStateFlow.value.map {
            it.second
        }

        val possibleNamesRegex = """${alias}(_\d+)?\$""".toRegex()
        val takenFileNames = savedArchivesNames.filter {
            possibleNamesRegex matches it
        }.sorted()

        return if (takenFileNames.isEmpty()) {
            alias
        } else {
            val suffixNumberRegex = """(?<=_)\d+\$""".toRegex()
            val suffixNumber =
                suffixNumberRegex.find(takenFileNames.last())?.value?.toIntOrNull() ?: 0
            "${alias}_${suffixNumber}"
        }
    }

    fun activateArchive(archiveFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            stopFileObservers()
            archiveCurrentDb()
            val cacheDbFile = File(cacheDir, "cache_db_file.db")
            archiveFile.renameTo(cacheDbFile)
            databaseImporter.importDb(cacheDbFile)
            cacheDbFile.delete()
            settingsDataStore.setCurrentDbAlias(archiveFile.nameWithoutExtension)
            startFileObservers()
        }
    }

    fun renameArchive(archiveFile: File, newName: String) {
        if (archiveFile.absolutePath == currentDbFile.absolutePath) {
            viewModelScope.launch {
                settingsDataStore.setCurrentDbAlias(newName)
            }
        } else {
            archiveFile.renameTo(File(archiveDir, createUniqueArchiveFilename(newName)))
        }
    }

    fun deleteArchive(archiveFile: File) {
        if (archiveFile.absolutePath == currentDbFile.absolutePath) {
            viewModelScope.launch(Dispatchers.IO) {
                dataRepository.clearAllTables()
                settingsDataStore.setCurrentDbAlias("")
            }
        } else {
            archiveFile.delete()
        }
    }
}