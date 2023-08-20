package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.os.FileObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.di.AppModule
import ir.demoodite.dakhlokharj.ui.components.activity.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
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
        MutableStateFlow<List<DatabaseArchiveListAdapter.DatabaseArchive>>(emptyList())
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
    private val fileListUpdateLock = AtomicBoolean(false)
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
    private val tempImportingFileFilename = "importing_temp.db"

    init {
        startFileObservers()
    }

    private fun updateFilesList() {
        if (fileListUpdateLock.get()) {
            return
        }
        val archivedFilesWithAlias = archiveDir.listFiles()?.map {
            DatabaseArchiveListAdapter.DatabaseArchive(it.nameWithoutExtension, it)
        } ?: emptyList()
        val currentArchive =
            DatabaseArchiveListAdapter.DatabaseArchive(currentDbAlias, dataRepository.dbFile)

        _allDbArchivesStateFlow.update {
            listOf(currentArchive).plus(archivedFilesWithAlias)
                .sortedByDescending { it.file.lastModified() }
        }
    }

    private fun startFileObservers() {
        currentDbFileObserver.startWatching()
        dbArchiveFileObserver.startWatching()
        updateFilesList()
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
        val possibleNamesRegex = """$alias( \(\d+\))?\$""".toRegex()
        val takenFileNames = _allDbArchivesStateFlow.value.filter {
            possibleNamesRegex matches it.alias
        }.map { it.alias }.sorted()

        return if (takenFileNames.isEmpty()) {
            alias
        } else {
            val suffixNumberRegex = """(?<= \()\d+(?=\)\$)""".toRegex()
            val suffixNumber =
                suffixNumberRegex.find(takenFileNames.last())?.value?.toIntOrNull() ?: 0
            "${alias}_${suffixNumber}"
        }
    }

    fun activateArchive(archiveFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            MainActivity.startLoading()
            fileListUpdateLock.set(true)
            archiveCurrentDb()
            settingsDataStore.setCurrentDbAlias(archiveFile.nameWithoutExtension)
            val cacheDbFile = File(cacheDir, "cache_db_file.db")
            archiveFile.renameTo(cacheDbFile)
            databaseImporter.importDb(cacheDbFile)
            cacheDbFile.delete()
            fileListUpdateLock.set(false)
            updateFilesList()
            MainActivity.stopLoading()
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

    fun savePendingFileToFileDescriptor(fileDescriptor: FileDescriptor) {
        viewModelScope.launch {
            try {
                FileOutputStream(fileDescriptor).use { outputStream ->
                    val pendingFile = File(cacheDir, "saving.db")
                    outputStream.write(pendingFile.readBytes())
                    MainActivity.sendMessage(R.string.saved_successfully)
                    pendingFile.deleteOnExit()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                MainActivity.sendMessage(R.string.operation_failed)
            }
        }
    }

    fun importInputStreamToCacheDir(inputStream: FileInputStream) {
        val tempFile = File(cacheDir, tempImportingFileFilename)
        tempFile.writeBytes(inputStream.readBytes())
    }

    suspend fun validateImportingDatabase(): Boolean {
        return try {
            val tempFile = File(cacheDir, tempImportingFileFilename)
            if (databaseImporter.isDatabaseValid(tempFile)) {
                true
            } else {
                MainActivity.sendError(R.string.invalid_database_file)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MainActivity.sendMessage(R.string.operation_failed)
            false
        }
    }

    fun applyTempDatabaseImport(alias: String) {
        try {
            val tempFile = File(cacheDir, tempImportingFileFilename)
            val importingFile = File(archiveDir, "$alias.db")
            importingFile.writeBytes(tempFile.readBytes())
            viewModelScope.launch {
                MainActivity.sendMessage(R.string.imported_successfully)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                MainActivity.sendMessage(R.string.operation_failed)
            }
        }
    }
}