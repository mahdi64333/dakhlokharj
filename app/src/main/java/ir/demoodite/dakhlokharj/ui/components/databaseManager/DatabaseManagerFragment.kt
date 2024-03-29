package ir.demoodite.dakhlokharj.ui.components.databaseManager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.databinding.ViewDialogDatabaseAliasBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.activity.MainActivity
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
    private fun RecyclerView.Adapter<ViewHolder>.asDatabaseArchiveListAdapter() =
        this as DatabaseArchiveListAdapter

    private val viewModel: DatabaseManagerViewModel by viewModels()

    /**
     * [ActivityResultLauncher] object to receive result of creating a new file intent to
     * save a file to user's phone.
     */
    private val createAndSaveFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    saveFileToUri(uri)
                }
            }
        }

    /**
     * [ActivityResultLauncher] object to receive result of selecting a file to import it to
     * application filesDir.
     */
    private val importFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    validateDbAndImport(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()

        val args: DatabaseManagerFragmentArgs by navArgs()
        // Import file from given uri to application archives if the argument is defined
        args.importingArchiveUri?.let {
            lifecycleScope.launch {
                withResumed {
                    validateDbAndImport(it)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatabaseArchiveUi()
        setupMenuProvider()
        overrideBackPressed()
    }

    private fun startFlowCollection() {
        // Database files collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dbArchivesStateFlow.collectLatest {
                    val adapter = binding.rvArchives.adapter as DatabaseArchiveListAdapter
                    adapter.submitList(it)
                }
            }
        }

        // Current database archive alias collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDbAliasStateFlow.collectLatest {
                    val adapter = binding.rvArchives.adapter as DatabaseArchiveListAdapter
                    adapter.activeArchiveAlias = it
                }
            }
        }

        // Listens for import requests
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getImportReceiver().collectLatest { uri ->
                    validateDbAndImport(uri)
                }
            }
        }
    }

    private fun setupDatabaseArchiveUi() {
        // Archives recyclerView adapter and layout manager
        binding.rvArchives.adapter = DatabaseArchiveListAdapter(
            activeArchiveAlias = viewModel.currentDbAlias,
            shareOnClickListener = {
                launchShareFileIntent(it)
            },
            saveOnClickListener = {
                launchSaveFileIntent(it.file, it.alias)
            },
            deleteOnClickListener = {
                showDeleteArchiveDialog(it.file)
            },
            activeArchiveOnClickListener = {
                viewModel.activateArchive(it.file)
            },
            newFilenameCallback = { archive, newName ->
                viewModel.renameArchive(
                    archive.file, newName
                )
            },
        )
        binding.rvArchives.layoutManager = LinearLayoutManager(requireContext())

        // Purchases recyclerView divider
        binding.rvArchives.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            )
        )
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
        // Create ViewBinding object of dialog layout
        val databaseAliasDialogBinding =
            ViewDialogDatabaseAliasBinding.inflate(layoutInflater, null, false)
        databaseAliasDialogBinding.textInputEditTextDatabaseAlias.filters = arrayOf(
            InputFilter.LengthFilter(24), FilenameInputFilter()
        )

        // Show "New archive" dialog
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.database_alias))
            .setMessage(getString(R.string.please_enter_database_alias))
            .setView(databaseAliasDialogBinding.root)
            .setPositiveButton(getString(R.string.confirm), null)
            .setNegativeButton(getString(R.string.cancel), null).show().apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val alias = validateDialogInputsAndGetAliasOrNull(databaseAliasDialogBinding)
                    if (alias != null) {
                        viewModel.newDatabaseArchive(alias)
                        dismiss()
                    }
                }
            }
    }

    private fun validateDialogInputsAndGetAliasOrNull(
        databaseAliasBinding: ViewDialogDatabaseAliasBinding,
    ): String? {
        var errorFlag = false

        val aliasText = databaseAliasBinding.textInputEditTextDatabaseAlias.text.toString().trim()

        if (aliasText.isEmpty()) {
            databaseAliasBinding.textInputLayoutDatabaseAlias.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(databaseAliasBinding.textInputEditTextDatabaseAlias)
            errorFlag = true
        }

        return if (errorFlag) null
        else aliasText
    }

    private fun launchShareFileIntent(archive: DatabaseArchiveListAdapter.DatabaseArchive) {
        // Temporary saving the file that's going to get shared to cacheDir
        val sharingCacheDir =
            File(requireContext().cacheDir, "sharing").also { if (!it.exists()) it.mkdir() }
        val sharingFile = File(sharingCacheDir, "${archive.alias}.dakhlokharj")
        sharingFile.writeBytes(archive.file.readBytes())

        // Launch an intent to share the pending file
        val fileUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.FileProvider", sharingFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(shareIntent)

        // Delete the file when sharing operation is ended
        sharingFile.deleteOnExit()
    }

    private fun launchSaveFileIntent(file: File, alias: String) {
        // Saving the file that's going to get shared to cacheDir
        val pendingFile = File(requireContext().cacheDir, "saving.db")
        pendingFile.writeBytes(file.readBytes())

        // Launch an intent to save the pending file
        val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3"
            putExtra(Intent.EXTRA_TITLE, "$alias.dakhlokharj")
        }
        createAndSaveFileActivityResultLauncher.launch(saveIntent)
    }

    private fun launceImportDatabaseIntent() {
        // Launch an intent to select a file
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

    /**
     * Override back press behaviour to stop editing if an archive is being edited.
     * If there is no edit in progress, it just navigates up.
     */
    private fun overrideBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.rvArchives.adapter?.asDatabaseArchiveListAdapter()?.isEditing() == true) {
                binding.rvArchives.adapter?.asDatabaseArchiveListAdapter()?.stopEditing()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Stops editing of database archive item if there is any edit in progress.
     */
    private fun stopEditing() {
        binding.rvArchives.adapter?.asDatabaseArchiveListAdapter()?.stopEditing()
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

    private fun validateDbAndImport(uri: Uri) {
        try {
            stopEditing()
            requireContext().contentResolver.openFileDescriptor(uri, "r")?.use {
                FileInputStream(it.fileDescriptor).use { inputStream ->
                    viewModel.importInputStreamToCacheDir(inputStream)
                    lifecycleScope.launch {
                        if (viewModel.validateImportingDatabase()) {
                            showImportDialog()
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

    private fun showImportDialog() {
        // Create ViewBinding object of dialog layout
        val databaseAliasDialogBinding =
            ViewDialogDatabaseAliasBinding.inflate(layoutInflater, null, false)
        databaseAliasDialogBinding.textInputEditTextDatabaseAlias.filters = arrayOf(
            InputFilter.LengthFilter(24), FilenameInputFilter()
        )

        // Show "Importing archive" dialog
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.database_alias))
            .setMessage(getString(R.string.please_enter_database_alias))
            .setView(databaseAliasDialogBinding.root)
            .setPositiveButton(getString(R.string.confirm), null)
            .setNegativeButton(getString(R.string.cancel), null).show().apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    validateDialogInputsAndGetAliasOrNull(databaseAliasDialogBinding)?.let { alias ->
                        viewModel.commitTempDatabaseImport(alias)
                        dismiss()
                    }
                }
            }
    }

    companion object ImportEventChannel {
        @Volatile
        private var INSTANCE: Channel<Uri>? = null

        private fun getChannel(): Channel<Uri> {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Channel()
                }
                INSTANCE!!
            }
        }

        private fun getImportReceiver(): Flow<Uri> {
            return getChannel().receiveAsFlow()
        }

        suspend fun importArchiveFromUri(uri: Uri) {
            getChannel().send(uri)
        }
    }
}