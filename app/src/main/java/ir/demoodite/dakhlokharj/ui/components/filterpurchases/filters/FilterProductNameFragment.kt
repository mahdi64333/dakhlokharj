package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterProductNameBinding
import ir.demoodite.dakhlokharj.ui.base.BasePurchaseFilteringFragment
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
        val productName = binding.textInputEditTextFilter.text.toString().trim()

        binding.textInputLayoutFilter.setEndIconOnClickListener {
            if (productName.isEmpty()) {
                binding.textInputLayoutFilter.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnType(binding.textInputEditTextFilter)
            } else {
                viewModel.filterByProductName(productName)
            }
        }
    }
}