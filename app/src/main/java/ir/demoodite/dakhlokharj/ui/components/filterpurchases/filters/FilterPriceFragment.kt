package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPriceBinding

@AndroidEntryPoint
class FilterPriceFragment : BasePurchaseFilteringFragment<FragmentFilterPriceBinding>(
    FilterBy.PRICE, FragmentFilterPriceBinding::inflate
) {
    override val filteredPurchasesRecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum
        get() = binding.tvPurchasesSum

    override fun setupFilterInput() {
        binding.textInputLayoutFilterMax.setEndIconOnClickListener {
            val minPriceText = binding.textInputEditTextFilterMin.getTextWithoutCommas()
            val maxPriceText = binding.textInputEditTextFilterMax.getTextWithoutCommas()

            val minPrice = if (minPriceText.isEmpty()) Double.MIN_VALUE else minPriceText.toDouble()
            val maxPrice = if (maxPriceText.isEmpty()) Double.MAX_VALUE else maxPriceText.toDouble()

            if (minPrice > maxPrice) {
                binding.tvError.isVisible = true
                binding.textInputLayoutFilterMin.error = " "
                binding.textInputLayoutFilterMax.error = " "
                binding.textInputEditTextFilterMin.addTextChangedListener {
                    clearError()
                }
                binding.textInputEditTextFilterMax.addTextChangedListener {
                    clearError()
                }
            } else {
                viewModel.filterByPrice(minPrice, maxPrice)
            }
        }
    }

    private fun clearError() {
        binding.tvError.isVisible = false
        binding.textInputLayoutFilterMin.error = null
        binding.textInputLayoutFilterMax.error = null
    }
}