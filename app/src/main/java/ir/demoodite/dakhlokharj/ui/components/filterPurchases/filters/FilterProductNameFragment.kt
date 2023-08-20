package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterProductNameBinding
import ir.demoodite.dakhlokharj.utils.UiUtil

@AndroidEntryPoint
class FilterProductNameFragment :
    BasePurchaseFilteringFragment<FragmentFilterProductNameBinding>(
        PurchaseFilters.PRODUCT_NAME, FragmentFilterProductNameBinding::inflate
    ) {
    // Setting values for abstract views to get used inside the parent class
    override val filteredPurchasesRecyclerView: RecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData: TextView
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum: TextView
        get() = binding.tvPurchasesSum

    override fun setupFilterInput() {
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            validateInputsAndGetProductNameOrNull()?.let { productName ->
                viewModel.filterByProductName(productName)
            }
        }
    }

    private fun validateInputsAndGetProductNameOrNull(): String? {
        var errorFlag = false
        val productName = binding.textInputEditTextFilter.text.toString().trim()

        // Product name validation
        if (productName.isEmpty()) {
            binding.textInputLayoutFilter.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
            errorFlag = true
        }

        return if (errorFlag) null
        else productName
    }
}