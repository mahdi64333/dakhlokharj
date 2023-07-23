package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPriceBinding
import ir.demoodite.dakhlokharj.ui.base.BasePurchaseFilteringFragment

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

            val minPrice = if (minPriceText.isEmpty()) Long.MIN_VALUE else minPriceText.toLong()
            val maxPrice = if (maxPriceText.isEmpty()) Long.MAX_VALUE else maxPriceText.toLong()

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