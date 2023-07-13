package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.adapters.DetailedPurchasesListAdapter
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.databinding.FragmentHomeBinding
import ir.demoodite.dakhlokharj.models.database.Purchase
import ir.demoodite.dakhlokharj.models.viewmodels.HomeViewModel
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var snackBar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupRecyclerView() {
        val decimalFormat =
            NumberFormat.getInstance(Locale(getString(R.string.language))) as DecimalFormat
        decimalFormat.applyPattern("#,###")
        val adapter = DetailedPurchasesListAdapter(decimalFormat) {
            UiUtil.setSweetAlertDialogNightMode(resources)
            lifecycleScope.launch(Dispatchers.IO) {
                SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE).apply {
                    titleText = getString(R.string.consumers)
                    val listView = ListView(requireContext())
                    val consumers =
                        DataRepository.getDatabase(requireContext()).consumerDao.getConsumerNamesOfPurchase(
                            it.purchaseId
                        ).first()
                    val arrayAdapter = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        consumers
                    )
                    listView.adapter = arrayAdapter
                    setCustomView(listView)
                    confirmText = getString(R.string.confirm)
                    show()
                    getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0)
                }
            }
        }
        binding.rvPurchases.adapter = adapter
        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchases.addItemDecoration(MaterialDividerItemDecoration(
            requireContext(), MaterialDividerItemDecoration.VERTICAL
        ).apply {
            isLastItemDecorated = false
        })
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.purchasesStateFlow.collectLatest {
                adapter.submitList(it)
                binding.tvNoData.isVisible = it.isEmpty()
            }
        }
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val purchasesListAdapter =
                    binding.rvPurchases.adapter as DetailedPurchasesListAdapter
                val detailedPurchases = LinkedList(purchasesListAdapter.currentList)
                val detailedPurchasePosition = viewHolder.adapterPosition
                val detailedPurchase = detailedPurchases[detailedPurchasePosition]
                detailedPurchases.removeAt(detailedPurchasePosition)
                purchasesListAdapter.submitList(detailedPurchases)
                snackBar?.dismiss()
                snackBar = Snackbar.make(
                    binding.root, getString(R.string.purchase_got_deleted), Snackbar.LENGTH_LONG
                )
                val snackBarCallBack = object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)

                        val purchase = detailedPurchase.let {
                            Purchase(
                                it.purchaseId,
                                it.purchaseProduct,
                                it.purchasePrice,
                                it.purchaseBuyerId,
                                it.purchaseTime
                            )
                        }
                        viewModel.deletePurchase(purchase)
                    }
                }
                snackBar?.apply {
                    setAction(R.string.undo) {
                        detailedPurchases.add(detailedPurchasePosition, detailedPurchase)
                        purchasesListAdapter.submitList(detailedPurchases)
                        binding.rvPurchases.adapter = purchasesListAdapter
                        removeCallback(snackBarCallBack)
                        dismiss()
                    }
                    addCallback(snackBarCallBack)
                    show()
                }
            }
        })
    }

    private fun setupFab() {
        binding.fabAddPurchase.setOnClickListener {

        }
    }
}