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
    private val viewModel: SummeryViewModel by viewModels()
    private lateinit var decimalFormat: DecimalFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
        setupFilteringState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        decimalFormat =
            NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat
        setupOptionsMenu()
        setupSummariesRecyclerView()
        setupFilteredSummariesRecyclerView()
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.residentsSummariesStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updateSummariesUi(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.filteredResidentsSummariesStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updateFilteredSummariesUi(it)
                }
            }
        }
    }

    private fun updateSummariesUi(residentSummaries: List<ResidentSummery>) {
        val residentSummariesListAdapter =
            binding.rvSummery.adapter as ResidentSummariesListAdapter
        binding.tvNoData.isVisible = residentSummaries.isEmpty()
        residentSummariesListAdapter.submitList(residentSummaries)
    }

    private fun updateFilteredSummariesUi(residentSummaries: List<ResidentSummery>?) {
        val filteredResidentSummariesListAdapter =
            binding.rvSummeryFiltered.adapter as ResidentSummariesListAdapter
        binding.tvFilteredNoData.isVisible = residentSummaries?.isEmpty() ?: false
        filteredResidentSummariesListAdapter.submitList(residentSummaries)
    }

    private fun setupFilteringState() {
        lifecycleScope.launch {
            viewModel.isFilteringStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    binding.layoutFilteredSummery.isVisible = it
                    binding.layoutSummery.isGone = it
                }
            }
        }
    }

    private fun toggleFiltering() {
        viewModel.toggleFiltering()
    }

    private fun setupSummariesRecyclerView() {
        val adapter = ResidentSummariesListAdapter(decimalFormat)
        binding.rvSummery.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                MaterialDividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).apply {
                    isLastItemDecorated = false
                }
            )
            this.adapter = adapter
        }
    }

    private fun setupFilteredSummariesRecyclerView() {
        val adapter = ResidentSummariesListAdapter(decimalFormat)
        binding.rvSummeryFiltered.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                MaterialDividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).apply {
                    isLastItemDecorated = false
                }
            )
            this.adapter = adapter
        }
        binding.textInputLayoutFilterMax.setEndIconOnClickListener {
            val validatedTimestamps = validateAndGetTimestamps()
            if (validatedTimestamps != null) {
                viewModel.setSummariesTimeWindow(
                    validatedTimestamps.first,
                    validatedTimestamps.second
                )
            }
        }
    }

    private fun validateAndGetTimestamps(): Pair<Long, Long>? {
        var errorFlag = false

        val minTimeText = binding.textInputEditTextFilterMin.text.toString()
        val minTimeRawText = binding.textInputEditTextFilterMin.rawText
        val maxTimeText = binding.textInputEditTextFilterMax.text.toString()
        val maxTimeRawText = binding.textInputEditTextFilterMax.rawText

        if (minTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMin.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        } else if (minTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(minTimeText)
        ) {
            binding.textInputLayoutFilterMin.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        }

        if (maxTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMax.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        } else if (maxTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(maxTimeText)
        ) {
            binding.textInputLayoutFilterMax.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        }

        if (errorFlag) {
            return null
        }

        return Pair(
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
                    R.id.action_filter -> {
                        toggleFiltering()
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