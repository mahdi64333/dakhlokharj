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
class FilterBuyerFragment : BasePurchaseFilteringFragment<FragmentFilterBuyerBinding>(
    PurchaseFilters.BUYER, FragmentFilterBuyerBinding::inflate
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
                    updateBuyerAutocompleteTextViewAdapter(residents)
                }
            }
        }
    }

    private fun updateBuyerAutocompleteTextViewAdapter(residents: List<Resident>) {
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
            validateInputsAndGetBuyerIdOrNull()?.let { buyerId ->
                viewModel.filterByBuyer(buyerId)
            }
        }
    }

    private fun validateInputsAndGetBuyerIdOrNull(): Long? {
        var errorFlag = false
        val buyerName = binding.autoCompleteTextViewFilter.text.toString()
        val buyerResident = viewModel.residents.find { it.name == buyerName }

        // Buyer name validation
        if (buyerName.isEmpty()) {
            binding.textInputLayoutFilter.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
            errorFlag = true
        } else if (buyerResident == null) {
            binding.textInputLayoutFilter.error = getString(R.string.no_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewFilter)
            errorFlag = true
        }

        return if (errorFlag) null
        else buyerResident!!.id
    }
}