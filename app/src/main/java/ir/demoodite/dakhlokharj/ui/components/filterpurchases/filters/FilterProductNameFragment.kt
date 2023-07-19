package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.databinding.FragmentFilterProductNameBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.FilterPurchasesViewModel
import ir.demoodite.dakhlokharj.ui.components.home.PurchasesListAdapter
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class FilterProductNameFragment :
    BaseFragment<FragmentFilterProductNameBinding>(FragmentFilterProductNameBinding::inflate) {
    private val viewModel: FilterPurchasesViewModel by activityViewModels()
    private lateinit var decimalFormat: DecimalFormat

    override fun onAttach(context: Context) {
        super.onAttach(context)

        decimalFormat =
            NumberFormat.getInstance(Locale(getString(R.string.language))) as DecimalFormat
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterInput()
        setupPurchasesRecyclerView()
    }

    private fun setupFilterInput() {
        binding.textInputLayoutFilter.setEndIconOnClickListener {
            val productName = binding.textInputEditTextFilter.text.toString().trim()

            if (productName.isEmpty()) {
                binding.textInputLayoutFilter.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnType(binding.textInputEditTextFilter)
            } else {
                viewModel.filterByProductName(productName)
            }
        }
    }

    private fun setupPurchasesRecyclerView() {
        val adapter = PurchasesListAdapter(decimalFormat) {
            UiUtil.setSweetAlertDialogNightMode(resources)
            lifecycleScope.launch {
                val consumers =
                    DataRepository.getDatabase(requireContext()).consumerDao.getConsumerNamesOfPurchase(
                        it.purchaseId
                    ).first()
                UiUtil.createConsumersSweetAlertDialog(requireContext(), consumers).apply {
                    show()
                    getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0)
                }
            }
        }
        binding.rvPurchasesFiltered.adapter = adapter
        binding.rvPurchasesFiltered.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchasesFiltered.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPurchasesStateFlow(FilterBy.PRODUCT_NAME).collectLatest {
                    withStarted {
                        it?.let {
                            adapter.submitList(it.first)
                            binding.tvPurchasesSum.text =
                                getString(R.string.purchases_sum, decimalFormat.format(it.second))
                            binding.tvFilteredNoData.isVisible = it.first.isEmpty()
                        }
                    }
                }
            }
        }
    }
}