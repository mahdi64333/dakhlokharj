package ir.demoodite.dakhlokharj.ui.components.databasemanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment

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
                        archiveCurrentDatabase()
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

    private fun archiveCurrentDatabase() {
        TODO()
    }

    private fun importDatabase() {
        TODO()
    }
}