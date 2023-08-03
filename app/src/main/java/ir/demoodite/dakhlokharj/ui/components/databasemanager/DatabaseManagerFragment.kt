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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.databinding.ViewDialogDatabaseAliasBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class DatabaseManagerFragment :
    BaseFragment<FragmentDatabaseManagerBinding>(FragmentDatabaseManagerBinding::inflate) {
    private val viewModel: DatabaseManagerViewModel by viewModels()
    private val createAndSaveFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    saveFileToUri(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuProvider()
        setupDatabaseArchiveUi()
    }

    private fun saveFile(file: File) {
        val pendingFile = File(requireContext().cacheDir, "saving.db")
        pendingFile.outputStream().use {
            it.write(file.readBytes())
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3"
            putExtra(Intent.EXTRA_TITLE, file.name)
        }

        createAndSaveFileActivityResultLauncher.launch(intent)
    }

    private fun saveFileToUri(uri: Uri) {
        try {
            requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outputStream ->
                    val pendingFile = File(requireContext().cacheDir, "saving.db")
                    outputStream.write(pendingFile.readBytes())
                    Snackbar.make(
                        binding.root,
                        getString(
                            R.string.saved_successfully
                        ),
                        Snackbar.LENGTH_LONG
                    ).show()
                    pendingFile.deleteOnExit()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                binding.root,
                getString(R.string.operation_failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun shareFile(file: File) {
        val sharingCacheDir =
            File(requireContext().cacheDir, "sharing").also { if (!it.exists()) it.mkdir() }
        val sharingFile = File(sharingCacheDir, file.name)
        sharingFile.outputStream().use {
            it.write(file.readBytes())
        }
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.FileProvider",
            sharingFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(shareIntent)
        sharingFile.deleteOnExit()
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
    }

    private fun setupDatabaseArchiveUi() {
        binding.rvArchives.adapter = DatabaseArchiveListAdapter(
            activeArchiveAlias = viewModel.currentDbAlias,
            shareOnClickListener = { shareFile(it) },
            saveOnClickListener = { saveFile(it) },
            deleteOnClickListener = { viewModel.deleteArchive(it) },
            activeArchiveOnClickListener = { viewModel.activateArchive(it) },
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
                        showArchiveCurrentDatabaseDialog()
                        true
                    }
                    R.id.action_import_database_from_file -> {
                        importDatabase()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun showArchiveCurrentDatabaseDialog() {
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
                    val alias = validateAndGetDatabaseAliasDialog(databaseAliasDialogBinding)
                    if (alias != null) {
                        newDatabaseArchive(alias)
                        dismiss()
                    }
                }
            }
    }

    private fun validateAndGetDatabaseAliasDialog(
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

    private fun newDatabaseArchive(alias: String) {
        viewModel.newDatabaseArchive(alias)
    }

    private fun importDatabase() {
        TODO()
    }
}