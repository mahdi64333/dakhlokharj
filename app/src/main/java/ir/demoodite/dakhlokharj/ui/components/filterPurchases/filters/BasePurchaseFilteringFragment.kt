package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterPurchases.FilterPurchasesViewModel
import ir.demoodite.dakhlokharj.ui.components.home.PurchasesListAdapter
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * A base [Fragment] class with support for view binding.
 *
 * @param T The [ViewBinding] class of fragment's layout
 * @param filterType [PurchaseFilters] object to query database based on it
 * @param inflateMethod Inflate method from the ViewBinding class of fragment's layout.
 * For example this parameter can be set with passing ```FragmentBinding::inflate```
 * to constructor when you want to use ```FragmentBinding``` as your ViewBinding class.
 */
abstract class BasePurchaseFilteringFragment<T : ViewBinding>(
    private val filterType: PurchaseFilters,
    inflateMethod: (LayoutInflater, ViewGroup?, Boolean) -> T,
) : BaseFragment<T>(inflateMethod) {
    protected fun RecyclerView.Adapter<ViewHolder>.asPurchasesListAdapter(): PurchasesListAdapter =
        this as PurchasesListAdapter

    /**
     * Shared [ViewModel] object to store data for all fragments inside ```filterPurchases```
     * package.
     */
    protected val viewModel: FilterPurchasesViewModel by activityViewModels()
    private val decimalFormat =
        NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat

    /**
     * [RecyclerView] to show filtered purchases inside it.
     */
    protected abstract val filteredPurchasesRecyclerView: RecyclerView

    /**
     * [TextView] object to be shown when there are no purchases after filtering with given input.
     */
    protected abstract val tvNoData: TextView

    /**
     * [TextView] object to show sum of the filtered purchases.
     */
    protected abstract val tvPurchasesPriceSum: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterInput()
        setupPurchasesRecyclerView()
    }

    private fun startFlowCollection() {
        // Collect filtered purchases based on filterType
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFilteredPurchasesStateFlow(filterType).collectLatest {
                    it?.let {
                        updateFilteredPurchasesUi(it.first, it.second)
                    }
                }
            }
        }
    }

    private fun updateFilteredPurchasesUi(purchases: List<DetailedPurchase>, priceSum: Double) {
        // Updating the list
        filteredPurchasesRecyclerView.adapter?.asPurchasesListAdapter()?.submitList(purchases)
        // Updating price sum
        tvPurchasesPriceSum.isVisible = purchases.isNotEmpty()
        tvPurchasesPriceSum.text =
            getString(R.string.purchases_sum, LocaleHelper.localizePrice(decimalFormat, priceSum))
        // Showing "No data" TextView if the list is empty
        tvNoData.isVisible = purchases.isEmpty()
    }

    protected abstract fun setupFilterInput()

    private fun setupPurchasesRecyclerView() {
        // Purchases recyclerView adapter and layout manager
        val adapter = PurchasesListAdapter() { detailedPurchase ->
            showConsumersListDialog(detailedPurchase)
        }
        adapter.onLongClickListener = { detailedPurchase ->
            showDeletePurchaseDialog(detailedPurchase)
        }
        filteredPurchasesRecyclerView.adapter = adapter
        filteredPurchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Purchases recyclerView divider
        filteredPurchasesRecyclerView.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            })
    }

    private fun showConsumersListDialog(detailedPurchase: DetailedPurchase) {
        UiUtil.setSweetAlertDialogNightMode(resources)
        lifecycleScope.launch {
            val consumers =
                DataRepository.getDatabase(requireContext()).consumerDao.getConsumerResidentsOfPurchase(
                    detailedPurchase.purchaseId
                ).first()
            UiUtil.createAndShowConsumersSweetDialog(requireContext(), consumers).apply {
                show()
                getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0)
            }
        }
    }

    private fun showDeletePurchaseDialog(detailedPurchase: DetailedPurchase) {
        UiUtil.setSweetAlertDialogNightMode(resources)
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE).apply {
            titleText = getString(R.string.are_you_sure_to_delete)
            confirmText = getString(R.string.yes)
            cancelText = getString(R.string.cancel)
            setConfirmClickListener {
                viewModel.requestPurchaseDelete(detailedPurchase.purchase)
                dismiss()
            }
            show()
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CONFIRM))
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CANCEL))
        }
    }
}