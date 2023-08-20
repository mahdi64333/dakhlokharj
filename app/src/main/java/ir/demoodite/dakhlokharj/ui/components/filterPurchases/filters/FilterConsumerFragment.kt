package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.FragmentFilterBuyerBinding
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterConsumerFragment :
    BasePurchaseFilteringFragment<FragmentFilterBuyerBinding>(
        PurchaseFilters.CONSUMER,
        FragmentFilterBuyerBinding::inflate
    ) {
    // Setting values for abstract views to get used inside the parent class
    override val filteredPurchasesRecyclerView: RecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData: TextView
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum: TextView
        get() = binding.tvPurchasesSum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    private fun startFlowCollection() {
        // Residents collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsStateFlow.collectLatest { residents ->
                    updateConsumerAutocompleteTextViewAdapter(residents)
                }
            }
        }
    }

    private fun updateConsumerAutocompleteTextViewAdapter(residents: List<Resident>) {
        val activeResidentNames = residents.map {
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
            validateInputsAndGetConsumerIdOrNull()?.let { consumerId ->
                viewModel.filterByConsumer(consumerId)
            }
        }
    }

    private fun validateInputsAndGetConsumerIdOrNull(): Long? {
        var errorFlag = false
        val consumerName = binding.autoCompleteTextViewFilter.text.toString()
        val consumerResident = viewModel.residents.find { it.name == consumerName }

        // Consumer name validation
        if (consumerName.isEmpty()) {
            binding.textInputLayoutFilter.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
            errorFlag = true
        } else if (consumerResident == null) {
            binding.textInputLayoutFilter.error = getString(R.string.no_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
            errorFlag = true
        }

        return if (errorFlag) null
        else consumerResident!!.id
    }
}