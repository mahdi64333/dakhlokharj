package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentSummeryBinding
import ir.demoodite.dakhlokharj.models.viewmodels.SummeryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SummeryFragment : Fragment() {
    private var _binding: FragmentSummeryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SummeryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSummeryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOptionsMenu()
        setupFilteringUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun toggleFiltering() {
        viewModel.toggleFiltering()
    }

    private fun setupFilteringUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFilteringStateFlow.collectLatest {
                    binding.textInputLayoutFilter.isVisible = it
                    binding.rvSummeryFiltered.isVisible = it
                    binding.rvSummery.isGone = it
                }
            }
        }
    }

    private fun setupOptionsMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.summery_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        toggleFiltering()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner)
    }
}