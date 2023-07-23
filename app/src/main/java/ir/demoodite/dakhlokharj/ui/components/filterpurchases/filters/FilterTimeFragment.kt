package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterTimeBinding
import ir.demoodite.dakhlokharj.ui.base.BasePurchaseFilteringFragment
import ir.demoodite.dakhlokharj.utils.DateUtil
import ir.demoodite.dakhlokharj.utils.UiUtil
import saman.zamani.persiandate.PersianDateFormat

@AndroidEntryPoint
class FilterTimeFragment :
    BasePurchaseFilteringFragment<FragmentFilterTimeBinding>(
        FilterBy.TIME,
        FragmentFilterTimeBinding::inflate
    ) {
    override val filteredPurchasesRecyclerView
        get() = binding.rvPurchasesFiltered
    override val tvNoData
        get() = binding.tvFilteredNoData
    override val tvPurchasesPriceSum
        get() = binding.tvPurchasesSum

    override fun setupFilterInput() {
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            if (binding.textInputEditTextFilter.rawText.length < 16) {
                binding.textInputLayoutFilter.error = getString(R.string.please_enter_valid_dates)
                UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
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
                        UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
                    } else {
                        viewModel.filterByTime(startDate.time, endDate.time)
                    }
                } catch (e: Exception) {
                    binding.textInputLayoutFilter.error =
                        getString(R.string.please_enter_valid_dates)
                    UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilter)
                }
            }
        }
    }
}