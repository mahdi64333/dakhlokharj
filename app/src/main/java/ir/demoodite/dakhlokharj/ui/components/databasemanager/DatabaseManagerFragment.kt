package ir.demoodite.dakhlokharj.ui.components.databasemanager

import androidx.fragment.app.viewModels
import ir.demoodite.dakhlokharj.databinding.FragmentDatabaseManagerBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment

class DatabaseManagerFragment :
    BaseFragment<FragmentDatabaseManagerBinding>(FragmentDatabaseManagerBinding::inflate) {
    private val viewModel: DatabaseManagerViewModel by viewModels()
}