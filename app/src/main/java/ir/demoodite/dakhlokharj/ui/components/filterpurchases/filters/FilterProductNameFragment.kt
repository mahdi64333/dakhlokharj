package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterProductNameBinding
import ir.demoodite.dakhlokharj.utils.UiUtil

@AndroidEntryPoint
class FilterProductNameFragment :
    BasePurchaseFilteringFragment<FragmentFilterProductNameBinding>(
        FilterBy.PRODUCT_NAME, FragmentFilterProductNameBinding::inflate
    ) {
    override val filteredPurchasesRecyclerView: RecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData: TextView
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum: TextView
        get() = binding.tvPurchasesSum

    override fun setupFilterInput() {
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            val productName = binding.textInputEditTextFilter.text.toString().trim()

            if (productName.isEmpty()) {
                binding.textInputLayoutFilter.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
            } else {
                viewModel.filterByProductName(productName)
            }
        }
    }
}