package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

import android.util.Range
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPriceBinding

@AndroidEntryPoint
class FilterPriceFragment : BasePurchaseFilteringFragment<FragmentFilterPriceBinding>(
    PurchaseFilters.PRICE, FragmentFilterPriceBinding::inflate
) {
    // Setting values for abstract views to get used inside the parent class
    override val filteredPurchasesRecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum
        get() = binding.tvPurchasesSum

    override fun setupFilterInput() {
        binding.textInputLayoutFilterMax.setEndIconOnClickListener {
            validateInputsAndReturnPriceRangeOrNull()?.let { priceRange ->
                viewModel.filterByPrice(priceRange.lower, priceRange.upper)
            }
        }
    }

    private fun validateInputsAndReturnPriceRangeOrNull(): Range<Double>? {
        var errorFlag = false

        val minPriceText = binding.textInputEditTextFilterMin.getTextWithoutCommas()
        val maxPriceText = binding.textInputEditTextFilterMax.getTextWithoutCommas()
        val minPrice = if (minPriceText.isEmpty()) Double.MIN_VALUE else minPriceText.toDouble()
        val maxPrice = if (maxPriceText.isEmpty()) Double.MAX_VALUE else maxPriceText.toDouble()

        // Price range validation
        val priceRange: Range<Double>? = try {
            Range(minPrice, maxPrice)
        } catch (e: IllegalArgumentException) {
            binding.tvError.isVisible = true
            binding.tvError.text = getString(R.string.min_price_is_larger_than_max)
            binding.textInputLayoutFilterMin.error = " "
            binding.textInputLayoutFilterMax.error = " "
            binding.textInputEditTextFilterMin.addTextChangedListener {
                clearError()
            }
            binding.textInputEditTextFilterMax.addTextChangedListener {
                clearError()
            }
            errorFlag = true
            null
        }

        return if (errorFlag) null
        else priceRange
    }

    private fun clearError() {
        binding.tvError.isVisible = false
        binding.textInputLayoutFilterMin.error = null
        binding.textInputLayoutFilterMax.error = null
    }
}