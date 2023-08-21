package ir.demoodite.dakhlokharj.ui.components.databaseManager

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
    // Base File objects
    private val archiveDir = File(filesDir, "archive").also {
        if (!it.exists()) it.mkdir()
    }
    val currentDbFile get() = dataRepository.dbFile

    // Archive StateFlows
    private val _dbArchivesStateFlow =
        MutableStateFlow<List<DatabaseArchiveListAdapter.DatabaseArchive>>(emptyList())
    val dbArchivesStateFlow get() = _dbArchivesStateFlow.asStateFlow()

    // File Observers
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

    /**
     * An [AtomicBoolean] to check if file observers are allowed to update archives list
     */
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

    private fun startFileObservers() {
        currentDbFileObserver.startWatching()
        dbArchiveFileObserver.startWatching()
        updateFilesList()
    }

    /**
     * Updates database archives.
     */
    private fun updateFilesList() {
        if (fileListUpdateLock.get()) {
            return
        }

        // Passive archives
        val archivedFilesWithAlias = archiveDir.listFiles()?.map {
            DatabaseArchiveListAdapter.DatabaseArchive(it.nameWithoutExtension, it)
        } ?: emptyList()
        // Active archive
        val currentArchive =
            DatabaseArchiveListAdapter.DatabaseArchive(currentDbAlias, dataRepository.dbFile)

        // Merging archives
        _dbArchivesStateFlow.update {
            listOf(currentArchive).plus(archivedFilesWithAlias)
                .sortedByDescending { it.file.lastModified() }
        }
    }

    /**
     * Archives the current database and creates an empty archive
     */
    fun newDatabaseArchive(newDatabaseArchiveAlias: String) {
        viewModelScope.launch(Dispatchers.IO) {
            archiveCurrentDb()

            // "Creating a new archive" by changing the active archive alias and clearing database tables
            settingsDataStore.setCurrentDbAlias(newDatabaseArchiveAlias)
            dataRepository.clearAllTables()
        }
    }

    /**
     * Archives the current active database. Saves it to [archiveDir].
     */
    private fun archiveCurrentDb() {
        val currentDbAlias = currentDbAlias.ifEmpty { "default" }
        val newArchiveFilename = createUniqueArchiveFilename(currentDbAlias)
        val newArchiveFile = File(archiveDir, "${newArchiveFilename}.db")
        newArchiveFile.writeBytes(dataRepository.dbFile.readBytes())
    }

    /**
     * Creates a unique archive name from [alias] as the base. Adds numbers to end
     * of the new alias if the name is taken.
     *
     * @param alias Base of the unique new alias
     */
    private fun createUniqueArchiveFilename(alias: String): String {
        // Finding taken names with alias as their base
        val possibleNamesRegex = """$alias( \(\d+\))?\$""".toRegex()
        val takenFileNames = _dbArchivesStateFlow.value.filter {
            possibleNamesRegex matches it.alias
        }.map { it.alias }.sorted()

        return if (takenFileNames.isEmpty()) {
            alias
        } else {
            val suffixNumberRegex = """(?<= \()\d+(?=\)\$)""".toRegex()
            val suffixNumber =
                suffixNumberRegex.find(takenFileNames.last())?.value?.toIntOrNull() ?: 0
            "${alias}_${suffixNumber}" // Constructed unique name
        }
    }

    /**
     * Activates a new archive file.
     *
     * @param archiveFile The file that's going to get activated.
     */
    fun activateArchive(archiveFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            MainActivity.startLoading()
            fileListUpdateLock.set(true)

            archiveCurrentDb()

            // Importing the database
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

    /**
     * Saves the file "saving.db" inside [cacheDir] to the given [FileDescriptor].
     */
    fun savePendingFileToFileDescriptor(fileDescriptor: FileDescriptor) {
        viewModelScope.launch {
            try {
                FileOutputStream(fileDescriptor).use { outputStream ->
                    val pendingFile = File(cacheDir, "saving.db")
                    outputStream.write(pendingFile.readBytes())
                    pendingFile.deleteOnExit()

                    MainActivity.sendMessage(R.string.saved_successfully)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                MainActivity.sendMessage(R.string.operation_failed)
            }
        }
    }

    /**
     * Creates a file inside [cacheDir] to import it with [commitTempDatabaseImport] after validation.
     */
    fun importInputStreamToCacheDir(inputStream: FileInputStream) {
        val tempImportingFile = File(cacheDir, tempImportingFileFilename)
        tempImportingFile.writeBytes(inputStream.readBytes())
    }

    /**
     * Validates the importing file inside [cacheDir].
     *
     * @return True if the file was valid and false otherwise
     */
    suspend fun validateImportingDatabase(): Boolean {
        return try {
            val tempImportingFile = File(cacheDir, tempImportingFileFilename)
            if (databaseImporter.isDatabaseValid(tempImportingFile)) {
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

    /**
     * Must be ran after [importInputStreamToCacheDir]. Commits importing of the importing file
     * inside [cacheDir].
     *
     * @param alias Alias for the new archive created from importing file.
     */
    fun commitTempDatabaseImport(alias: String) {
        try {
            val tempImportingFile = File(cacheDir, tempImportingFileFilename)
            val importingFile = File(archiveDir, "$alias.db")
            importingFile.writeBytes(tempImportingFile.readBytes())
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