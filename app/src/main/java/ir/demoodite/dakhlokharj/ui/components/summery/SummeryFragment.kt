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
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            if (binding.textInputEditTextFilter.rawText.length < 16) {
                binding.textInputLayoutFilter.error = getString(R.string.please_enter_valid_dates)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
            } else {
                val datesText =
                    binding.textInputEditTextFilter.text.toString().removePrefix("From ")
                val datesTextList = datesText.split(" to ")
                try {
                    datesTextList.forEach {
                        if (!LocaleHelper.validateLocalizedDate(it)) {
                            throw Exception()
                        }
                    }
                    val startDate = LocaleHelper.parseLocalizedDate(datesTextList.first())
                    val endDate = LocaleHelper.parseLocalizedDate(datesTextList.last())
                    endDate.addDay(1)
                    if (startDate > endDate) {
                        binding.textInputLayoutFilter.error =
                            getString(R.string.please_enter_valid_dates)
                        UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
                    } else {
                        viewModel.setSummariesTimeWindow(startDate.time, endDate.time)
                    }
                } catch (e: Exception) {
                    binding.textInputLayoutFilter.error =
                        getString(R.string.please_enter_valid_dates)
                    UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
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