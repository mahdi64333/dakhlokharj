package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

import android.util.Range
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterTimeBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil

@AndroidEntryPoint
class FilterTimeFragment :
    BasePurchaseFilteringFragment<FragmentFilterTimeBinding>(
        PurchaseFilters.TIME,
        FragmentFilterTimeBinding::inflate
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
            validateAndGetTimestampRangeOrNull()?.let { timestampRange ->
                viewModel.filterByTime(timestampRange.lower, timestampRange.upper)
            }
        }
    }

    private fun validateAndGetTimestampRangeOrNull(): Range<Long>? {
        var errorFlag = false

        val startTimeText = binding.textInputEditTextFilterMin.text.toString()
        val startTimeRawText = binding.textInputEditTextFilterMin.rawText
        val endTimeText = binding.textInputEditTextFilterMax.text.toString()
        val endTimeRawText = binding.textInputEditTextFilterMax.rawText

        // Start time validation
        if (startTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMin.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        } else if (startTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(startTimeText)
        ) {
            binding.textInputLayoutFilterMin.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            errorFlag = true
        }

        // End time validation
        if (endTimeRawText.isEmpty()) {
            binding.textInputLayoutFilterMax.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        } else if (endTimeRawText.length != 8
            || !LocaleHelper.validateLocalizedDate(endTimeText)
        ) {
            binding.textInputLayoutFilterMax.error = getString(R.string.please_enter_a_valid_date)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
        }

        // Timestamp range validation
        val timestampRange: Range<Long>? = try {
            Range(
                LocaleHelper.parseLocalizedDate(startTimeText).time,
                LocaleHelper.parseLocalizedDate(endTimeText).addDay(1).time
            )
        } catch (e: IllegalArgumentException) {
            binding.textInputLayoutFilterMin.error =
                getString(R.string.start_time_is_larger_than_end)
            binding.textInputLayoutFilterMax.error = " "
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMin)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextFilterMax)
            errorFlag = true
            null
        }

        return if (errorFlag) null
        else timestampRange
    }
}