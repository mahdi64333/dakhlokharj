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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.databinding.ViewDialogDatabaseAliasBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.utils.UiUtil

@AndroidEntryPoint
class DatabaseManagerFragment :
    BaseFragment<FragmentDatabaseManagerBinding>(FragmentDatabaseManagerBinding::inflate) {
    private val viewModel: DatabaseManagerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuProvider()
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.database_archive_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_archive_current_database -> {
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
            InputFilter.LengthFilter(24),
            FilenameInputFilter()
        )

        MaterialAlertDialogBuilder(
            requireContext()
        ).setTitle(getString(R.string.database_alias))
            .setMessage(getString(R.string.please_enter_database_alias))
            .setView(databaseAliasDialogBinding.root)
            .setPositiveButton(getString(R.string.confirm), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .show().apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val alias = validateAndGetDatabaseAliasDialog(databaseAliasDialogBinding)
                    if (alias != null) {
                        archiveCurrentDatabase(alias)
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

    private fun archiveCurrentDatabase(alias: String) {
        TODO()
    }

    private fun importDatabase() {
        TODO()
    }
}