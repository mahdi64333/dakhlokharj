package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.databinding.ViewDialogDatabaseAliasBinding
import ir.demoodite.dakhlokharj.eventsystem.file.FileEventChannel
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DatabaseManagerFragment :
    BaseFragment<FragmentDatabaseManagerBinding>(FragmentDatabaseManagerBinding::inflate) {
    private val viewModel: DatabaseManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuProvider()
        setupDatabaseArchiveUi()
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
            shareOnClickListener = {
                lifecycleScope.launch {
                    FileEventChannel.getSender().send(
                        FileEventChannel.FileEvent(
                            FileEventChannel.FileEventType.SHARE_FILE,
                            it,
                        )
                    )
                }
            },
            saveOnClickListener = {
                lifecycleScope.launch {
                    FileEventChannel.getSender().send(
                        FileEventChannel.FileEvent(
                            FileEventChannel.FileEventType.SAVE_FILE,
                            it,
                        )
                    )
                }
            },
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