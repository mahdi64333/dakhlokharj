package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterTimeBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil

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
        binding.textInputLayoutFilterMax.setEndIconOnClickListener {
            val validatedTimestamps = validateAndGetTimestamps()
            if (validatedTimestamps != null) {
                viewModel.filterByTime(validatedTimestamps.first, validatedTimestamps.second)
            }
        }
    }

    private fun validateAndGetTimestamps(): Pair<Long, Long>? {
        var errorFlag = false

        val minTimeText = binding.textInputEditTextFilterMin.text.toString()
        val minTimeRawText = binding.textInputEditTextFilterMin.rawText
        val maxTimeText = binding.textInputEditTextFilterMax.text.toString()
        val maxTimeRawText = binding.textInputEditTextFilterMax.rawText

        if (minTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMin.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        } else if (minTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(minTimeText)
        ) {
            binding.textInputLayoutFilterMin.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        }

        if (maxTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMax.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        } else if (maxTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(maxTimeText)
        ) {
            binding.textInputLayoutFilterMax.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        }

        if (errorFlag) {
            return null
        }

        return Pair(
            LocaleHelper.parseLocalizedDate(minTimeText).time,
            LocaleHelper.parseLocalizedDate(maxTimeText).time
        )
    }
}