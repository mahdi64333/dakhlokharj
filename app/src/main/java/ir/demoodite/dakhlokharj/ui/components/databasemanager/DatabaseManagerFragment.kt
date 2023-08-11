package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.databinding.ViewDialogDatabaseAliasBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.mainactivity.MainActivity
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

@AndroidEntryPoint
class DatabaseManagerFragment :
    BaseFragment<FragmentDatabaseManagerBinding>(FragmentDatabaseManagerBinding::inflate) {
    private val viewModel: DatabaseManagerViewModel by viewModels()
    private val createAndSaveFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    saveFileToUri(uri)
                }
            }
        }
    private val importFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    validateDbAndShowImportArchiveDatabaseDialog(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
        val args: DatabaseManagerFragmentArgs by navArgs()

        args.importingArchiveUri?.let {
            validateDbAndShowImportArchiveDatabaseDialog(it)
        }
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allDbArchivesStateFlow.collectLatest {
                    val adapter = binding.rvArchives.adapter as DatabaseArchiveListAdapter
                    adapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDbAliasStateFlow.collectLatest {
                    val adapter = binding.rvArchives.adapter as DatabaseArchiveListAdapter
                    adapter.activeArchiveAlias = it.ifEmpty { getString(R.string.app_name) }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                importChannelReceiver.collectLatest { (event, uri) ->
                    if (event == ImportEvents.IMPORT) {
                        validateDbAndShowImportArchiveDatabaseDialog(uri)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuProvider()
        setupDatabaseArchiveUi()
    }

    private fun saveFileToUri(uri: Uri) {
        try {
            requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                viewModel.savePendingFileToFileDescriptor(it.fileDescriptor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lifecycleScope.launch {
                MainActivity.sendMessage(R.string.operation_failed)
            }
        }
    }

    private fun setupDatabaseArchiveUi() {
        binding.rvArchives.adapter = DatabaseArchiveListAdapter(
            activeArchiveAlias = viewModel.currentDbAlias,
            shareOnClickListener = { file, alias ->
                stopEditing()
                launchShareFileIntent(file, alias)
            },
            saveOnClickListener = { file, alias ->
                stopEditing()
                launchSaveFileIntent(file, alias)
            },
            deleteOnClickListener = {
                stopEditing()
                showDeleteArchiveDialog(it)
            },
            activeArchiveOnClickListener = {
                stopEditing()
                viewModel.activateArchive(it)
            },
            newFilenameCallback = { file, newName -> viewModel.renameArchive(file, newName) },
        )
        binding.rvArchives.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArchives.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.database_archive_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_new_archive -> {
                        showNewArchiveDatabaseDialog()
                        true
                    }
                    R.id.action_import_database_from_file -> {
                        launceImportDatabaseIntent()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun showNewArchiveDatabaseDialog() {
        stopEditing()
        val databaseAliasDialogBinding =
            ViewDialogDatabaseAliasBinding.inflate(layoutInflater, null, false)
        databaseAliasDialogBinding.textInputEditTextDatabaseAlias.filters = arrayOf(
            InputFilter.LengthFilter(24), FilenameInputFilter()
        )

        MaterialAlertDialogBuilder(
            requireContext()
        ).setTitle(getString(R.string.database_alias))
            .setMessage(getString(R.string.please_enter_database_alias))
            .setView(databaseAliasDialogBinding.root)
            .setPositiveButton(getString(R.string.confirm), null)
            .setNegativeButton(getString(R.string.cancel), null).show().apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val alias =
                        validateAndGetDatabaseAliasFromDialogBinding(databaseAliasDialogBinding)
                    if (alias != null) {
                        viewModel.newDatabaseArchive(alias)
                        dismiss()
                    }
                }
            }
    }

    private fun validateDbAndShowImportArchiveDatabaseDialog(uri: Uri) {
        try {
            stopEditing()
            requireContext().contentResolver.openFileDescriptor(uri, "r")?.use {
                FileInputStream(it.fileDescriptor).use { inputStream ->
                    viewModel.importInputStreamToCacheDir(inputStream)
                    lifecycleScope.launch {
                        if (viewModel.validateImportingDatabase()) {
                            val databaseAliasDialogBinding =
                                ViewDialogDatabaseAliasBinding.inflate(layoutInflater, null, false)
                            databaseAliasDialogBinding.textInputEditTextDatabaseAlias.filters =
                                arrayOf(
                                    InputFilter.LengthFilter(24), FilenameInputFilter()
                                )

                            MaterialAlertDialogBuilder(
                                requireContext()
                            ).setTitle(getString(R.string.database_alias))
                                .setMessage(getString(R.string.please_enter_database_alias))
                                .setView(databaseAliasDialogBinding.root)
                                .setPositiveButton(getString(R.string.confirm), null)
                                .setNegativeButton(getString(R.string.cancel), null).show().apply {
                                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                        val alias = validateAndGetDatabaseAliasFromDialogBinding(
                                            databaseAliasDialogBinding
                                        )
                                        if (alias != null) {
                                            viewModel.applyTempDatabaseImport(alias)
                                            dismiss()
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lifecycleScope.launch {
                MainActivity.sendMessage(R.string.operation_failed)
            }
        }
    }

    private fun validateAndGetDatabaseAliasFromDialogBinding(
        databaseAliasBinding: ViewDialogDatabaseAliasBinding,
    ): String? {
        var errorFlag = false

        val aliasText = databaseAliasBinding.textInputEditTextDatabaseAlias.text.toString().trim()

        if (aliasText.isEmpty()) {
            databaseAliasBinding.textInputLayoutDatabaseAlias.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(databaseAliasBinding.textInputEditTextDatabaseAlias)
            errorFlag = true
        }

        return if (errorFlag) null else aliasText
    }

    private fun showDeleteArchiveDialog(archive: File) {
        stopEditing()
        UiUtil.setSweetAlertDialogNightMode(resources)
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE).apply {
            contentText = getString(R.string.are_you_sure_to_delete_archive)
            confirmText = getString(R.string.confirm)
            setConfirmClickListener {
                viewModel.deleteArchive(archive)
                dismiss()
            }
            cancelText = getString(R.string.cancel)
            show()
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CONFIRM))
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CANCEL))
        }
    }

    private fun launchShareFileIntent(file: File, alias: String) {
        val sharingCacheDir =
            File(requireContext().cacheDir, "sharing").also { if (!it.exists()) it.mkdir() }
        val sharingFile = File(sharingCacheDir, "$alias.dakhlokharj")
        sharingFile.outputStream().use {
            it.write(file.readBytes())
        }
        val fileUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.FileProvider", sharingFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(shareIntent)
        sharingFile.deleteOnExit()
    }

    private fun launchSaveFileIntent(file: File, alias: String) {
        val pendingFile = File(requireContext().cacheDir, "saving.db")
        pendingFile.outputStream().use {
            it.write(file.readBytes())
        }

        val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3"
            putExtra(Intent.EXTRA_TITLE, "$alias.dakhlokharj")
        }

        createAndSaveFileActivityResultLauncher.launch(saveIntent)
    }

    private fun launceImportDatabaseIntent() {
        val openIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/octet-stream",
                    "application/x-sqlite3",
                    "application/vnd.sqlite3",
                )
            )
        }

        importFileActivityResultLauncher.launch(openIntent)
    }

    private fun stopEditing() {
        val adapter = binding.rvArchives.adapter as DatabaseArchiveListAdapter
        adapter.stopEditing()
    }

    companion object EventChannel {
        @Volatile
        private var IMPORT_CHANNEL_INSTANCE: Channel<Pair<ImportEvents, Uri>>? = null

        private val importChannel: Channel<Pair<ImportEvents, Uri>>
            get() {
                return IMPORT_CHANNEL_INSTANCE ?: synchronized(this) {
                    if (IMPORT_CHANNEL_INSTANCE == null) {
                        IMPORT_CHANNEL_INSTANCE = Channel()
                    }
                    IMPORT_CHANNEL_INSTANCE!!
                }
            }

        private val importChannelReceiver: Flow<Pair<ImportEvents, Uri>>
            get() {
                return importChannel.receiveAsFlow()
            }

        suspend fun importArchiveFromUri(uri: Uri) {
            importChannel.send(Pair(ImportEvents.IMPORT, uri))
        }

        enum class ImportEvents {
            IMPORT
        }
    }
}