package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.FilterPurchasesViewModel
import ir.demoodite.dakhlokharj.ui.components.home.PurchasesListAdapter
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

abstract class BasePurchaseFilteringFragment<T : ViewBinding>(
    private val filterType: FilterBy,
    inflateMethod: (LayoutInflater, ViewGroup?, Boolean) -> T,
) : BaseFragment<T>(inflateMethod) {
    protected val viewModel: FilterPurchasesViewModel by activityViewModels()
    private lateinit var decimalFormat: DecimalFormat

    protected abstract val filteredPurchasesRecyclerView: RecyclerView
    protected abstract val tvNoData: TextView
    protected abstract val tvPurchasesPriceSum: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        decimalFormat =
            NumberFormat.getInstance(LocaleHelper.getCurrentLocale(resources.configuration)) as DecimalFormat
        startDataCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterInput()
        setupPurchasesRecyclerView()
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.getFilteredPurchasesStateFlow(filterType).collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    it?.let {
                        updateFilteredPurchasesUi(it.first, it.second)
                        viewModel.notifyFilteredPurchasesChanged(filterType)
                    }
                }
            }
        }
    }

    private fun updateFilteredPurchasesUi(purchases: List<DetailedPurchase>, priceSum: Long) {
        val filteredPurchasesListAdapter =
            filteredPurchasesRecyclerView.adapter as PurchasesListAdapter
        filteredPurchasesListAdapter.submitList(purchases)
        tvPurchasesPriceSum.text =
            getString(R.string.purchases_sum, decimalFormat.format(priceSum))
        tvNoData.isVisible = purchases.isEmpty()
    }

    protected abstract fun setupFilterInput()

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
        filteredPurchasesRecyclerView.adapter = adapter
        filteredPurchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        filteredPurchasesRecyclerView.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            })
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val purchasesListAdapter =
                    filteredPurchasesRecyclerView.adapter as PurchasesListAdapter
                viewModel.requestPurchaseDelete(
                    purchasesListAdapter.currentList[viewHolder.adapterPosition].purchase
                )
            }
        }).attachToRecyclerView(filteredPurchasesRecyclerView)
    }
}