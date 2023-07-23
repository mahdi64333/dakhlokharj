package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterBuyerBinding
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterBuyerFragment :
    BasePurchaseFilteringFragment<FragmentFilterBuyerBinding>(
        FilterBy.BUYER,
        FragmentFilterBuyerBinding::inflate
    ) {
    override val filteredPurchasesRecyclerView: RecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData: TextView
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum: TextView
        get() = binding.tvPurchasesSum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.residentsStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updateBuyerAutocompleteTextViewAdapter()
                }
            }
        }
    }

    private fun updateBuyerAutocompleteTextViewAdapter() {
        val activeResidentNames = viewModel.residents.map {
            it.name
        }
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, activeResidentNames
        )
        binding.autoCompleteTextViewFilter.setAdapter(arrayAdapter)
    }

    override fun setupFilterInput() {
        binding.autoCompleteTextViewFilter.setOnClickListener {
            binding.autoCompleteTextViewFilter.showDropDown()
        }
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            val buyerName = binding.autoCompleteTextViewFilter.text.toString()

            if (buyerName.isEmpty()) {
                binding.textInputLayoutFilter.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
                return@setEndIconOnClickListener
            }
            if (viewModel.residents.find { it.name == buyerName } == null) {
                binding.textInputLayoutFilter.error = getString(R.string.no_residents_found)
                UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
                return@setEndIconOnClickListener
            }

            viewModel.filterByBuyer(
                (viewModel.residents.find { it.name == buyerName })?.id ?: -1
            )
        }
    }
}