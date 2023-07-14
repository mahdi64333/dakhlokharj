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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.adapters.ResidentSummeryListAdapter
import ir.demoodite.dakhlokharj.databinding.FragmentSummeryBinding
import ir.demoodite.dakhlokharj.models.viewmodels.SummeryViewModel
import ir.demoodite.dakhlokharj.utils.DateUtil
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class SummeryFragment : Fragment() {
    private var _binding: FragmentSummeryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SummeryViewModel by viewModels()
    private lateinit var decimalFormat: DecimalFormat

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

        decimalFormat =
            NumberFormat.getInstance(Locale(getString(R.string.language))) as DecimalFormat
        setupOptionsMenu()
        setupFilteringUi()
        setupSummariesRecyclerView()
        setupFilteredSummariesRecyclerView()
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
                    binding.layoutFilteredSummery.isVisible = it
                    binding.layoutSummery.isGone = it
                }
            }
        }
    }

    private fun setupSummariesRecyclerView() {
        val adapter = ResidentSummeryListAdapter(decimalFormat)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsSummariesStateFlow.collectLatest {
                    binding.tvNoData.isVisible = it.isEmpty()
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun setupFilteredSummariesRecyclerView() {
        val adapter = ResidentSummeryListAdapter(decimalFormat)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredResidentsSummariesStateFlow.collectLatest {
                    binding.tvFilteredNoData.isVisible = it.isEmpty()
                    adapter.submitList(it)
                }
            }
        }
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            if (binding.textInputEditTextFilter.rawText.length < 16) {
                binding.textInputLayoutFilter.error = getString(R.string.please_enter_valid_dates)
                UiUtil.removeErrorOnType(binding.textInputEditTextFilter)
            } else {
                val datesText =
                    binding.textInputEditTextFilter.text.toString().removePrefix("From ")
                val datesTextList = datesText.split(" to ")
                val persianDateFormat = PersianDateFormat()
                try {
                    datesTextList.forEach {
                        if (!DateUtil.validateDate(it)) {
                            throw Exception()
                        }
                    }
                    val startDate = persianDateFormat.parse(datesTextList.first(), "yyyy/MM/dd")
                    val endDate = persianDateFormat.parse(datesTextList.last(), "yyyy/MM/dd")
                    endDate.addDay(1)
                    if (startDate > endDate) {
                        binding.textInputLayoutFilter.error =
                            getString(R.string.please_enter_valid_dates)
                        UiUtil.removeErrorOnType(binding.textInputEditTextFilter)
                    } else {
                        viewModel.setSummariesTimeWindow(startDate.time, endDate.time)
                    }
                } catch (e: Exception) {
                    binding.textInputLayoutFilter.error =
                        getString(R.string.please_enter_valid_dates)
                    UiUtil.removeErrorOnType(binding.textInputEditTextFilter)
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