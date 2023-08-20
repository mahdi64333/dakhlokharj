package ir.demoodite.dakhlokharj.ui.components.summery

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.ResidentSummery
import ir.demoodite.dakhlokharj.databinding.FragmentSummeryBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

@AndroidEntryPoint
class SummeryFragment : BaseFragment<FragmentSummeryBinding>(FragmentSummeryBinding::inflate) {
    private fun RecyclerView.Adapter<ViewHolder>.asResidentSummariesListAdapter() =
        this as ResidentSummariesListAdapter

    private val viewModel: SummeryViewModel by viewModels()
    private lateinit var decimalFormat: DecimalFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        decimalFormat = NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat
        setupOptionsMenu()
        setupSummariesRecyclerView()
        setupFilteredSummariesRecyclerViewAndInputs()
    }

    private fun startFlowCollection() {
        // Resident summaries collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsSummariesStateFlow.collectLatest { newSummaries ->
                    updateSummariesUi(newSummaries)
                }
            }
        }

        // Filtered resident summaries collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredResidentsSummariesStateFlow.collectLatest { newFilteredSummaries ->
                    updateFilteredSummariesUi(newFilteredSummaries)
                }
            }
        }

        // Filter state collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFilteringStateFlow.collectLatest { isFiltering ->
                    binding.layoutFilteredSummery.isVisible = isFiltering
                    binding.layoutSummery.isGone = isFiltering
                }
            }
        }
    }

    private fun updateSummariesUi(residentSummaries: List<ResidentSummery>) {
        binding.tvNoData.isVisible = residentSummaries.isEmpty()
        binding.rvSummery.adapter?.asResidentSummariesListAdapter()?.submitList(residentSummaries)
    }

    private fun updateFilteredSummariesUi(residentSummaries: List<ResidentSummery>?) {
        binding.tvFilteredNoData.isVisible = residentSummaries?.isEmpty() ?: false
        binding.rvSummeryFiltered.adapter?.asResidentSummariesListAdapter()
            ?.submitList(residentSummaries)
    }

    private fun setupSummariesRecyclerView() {
        // Resident summaries recyclerView adapter, layout manager and item divider
        binding.rvSummery.apply {
            this.adapter = ResidentSummariesListAdapter()
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(MaterialDividerItemDecoration(
                requireContext(), DividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            })
        }
    }

    private fun setupFilteredSummariesRecyclerViewAndInputs() {
        // Filtered resident summaries recyclerView adapter, layout manager and item divider
        binding.rvSummery.apply {
            this.adapter = ResidentSummariesListAdapter()
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(MaterialDividerItemDecoration(
                requireContext(), DividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            })
        }

        // Filter range input Setup
        binding.textInputLayoutFilterMax.setEndIconOnClickListener {
            validateAndGetTimestampsOrNull()?.let { (filterStart, filterEnd) ->
                viewModel.setSummariesTimeWindow(filterStart, filterEnd)
            }
        }
    }

    private fun validateAndGetTimestampsOrNull(): Pair<Long, Long>? {
        var errorFlag = false

        val minTimeText = binding.textInputEditTextFilterMin.text.toString()
        val minTimeRawText = binding.textInputEditTextFilterMin.rawText
        val maxTimeText = binding.textInputEditTextFilterMax.text.toString()
        val maxTimeRawText = binding.textInputEditTextFilterMax.rawText

        // Minimum time input validation
        if (minTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMin.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        } else if (minTimeRawText.length != 8 || !LocaleHelper.validateLocalizedDate(minTimeText)) {
            binding.textInputLayoutFilterMin.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        }

        // Maximum time input validation
        if (maxTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMax.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        } else if (maxTimeRawText.length != 8 || !LocaleHelper.validateLocalizedDate(maxTimeText)) {
            binding.textInputLayoutFilterMax.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        }

        return if (errorFlag) null
        else Pair(
            LocaleHelper.parseLocalizedDate(minTimeText).time,
            LocaleHelper.parseLocalizedDate(maxTimeText).addDay(1).time
        )
    }

    private fun setupOptionsMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.summery_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // Item for toggling between all summaries and filtered summaries
                    R.id.action_filter -> {
                        viewModel.toggleFiltering()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }
}