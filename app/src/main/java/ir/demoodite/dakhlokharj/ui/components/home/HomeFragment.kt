package ir.demoodite.dakhlokharj.ui.components.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.settings.enums.OrderBy
import ir.demoodite.dakhlokharj.databinding.FragmentHomeBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.addpurchase.AddPurchaseBottomSheetFragment
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var orderMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLanguageSelectionIfFirstLaunch()
        setupRecyclerView()
        setupFab()
        setupOptionsMenu()
    }


    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.purchasesStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updatePurchasesUi(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.orderStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updateOrderMenuItemIcon(OrderBy.valueOf(it))
                }
            }
        }
    }

    private fun updatePurchasesUi(detailedPurchases: List<DetailedPurchase>) {
        val purchasesListAdapter =
            binding.rvPurchases.adapter as PurchasesListAdapter
        purchasesListAdapter.submitList(detailedPurchases)
        binding.tvNoData.isVisible = detailedPurchases.isEmpty()
    }

    private fun updateOrderMenuItemIcon(orderBy: OrderBy) {
        try {
            when (orderBy) {
                OrderBy.TIME_ASC -> orderMenuItem.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_order_time_asc,
                        requireContext().theme
                    )
                OrderBy.TIME_DESC -> orderMenuItem.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_order_time_desc,
                        requireContext().theme
                    )
                OrderBy.PRICE_ASC -> orderMenuItem.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_order_price_asc,
                        requireContext().theme
                    )
                OrderBy.PRICE_DESC -> orderMenuItem.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_order_price_desc,
                        requireContext().theme
                    )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLanguageSelectionIfFirstLaunch() {
        runBlocking {
            if (viewModel.noLanguageSelected()) {
                val action = HomeFragmentDirections.actionHomeFragmentToLanguageSelectionFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupRecyclerView() {
        val decimalFormat =
            NumberFormat.getInstance(LocaleHelper.currentLocale) as DecimalFormat
        decimalFormat.applyPattern("#,###")
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
        binding.rvPurchases.adapter = adapter
        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchases.addItemDecoration(MaterialDividerItemDecoration(
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
                val purchasesListAdapter = binding.rvPurchases.adapter as PurchasesListAdapter
                val detailedPurchases = LinkedList(purchasesListAdapter.currentList)
                val detailedPurchasePosition = viewHolder.adapterPosition
                val detailedPurchase = detailedPurchases[detailedPurchasePosition]
                detailedPurchases.removeAt(detailedPurchasePosition)
                purchasesListAdapter.submitList(detailedPurchases)
                Snackbar.make(
                    binding.root, getString(R.string.purchase_got_deleted), Snackbar.LENGTH_LONG
                ).apply {
                    setAction(R.string.undo) {
                        detailedPurchases.add(detailedPurchasePosition, detailedPurchase)
                        purchasesListAdapter.submitList(detailedPurchases)
                        binding.rvPurchases.adapter = purchasesListAdapter
                    }
                    addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deletePurchase(detailedPurchase.purchase)
                            }
                        }
                    })
                    show()
                }
            }
        }).attachToRecyclerView(binding.rvPurchases)
    }

    private fun setupFab() {
        binding.fabAddPurchase.setOnClickListener {
            val addPurchaseBottomSheetFragment = AddPurchaseBottomSheetFragment()
            addPurchaseBottomSheetFragment.show(
                requireActivity().supportFragmentManager, AddPurchaseBottomSheetFragment.TAG
            )
        }
    }

    private fun setupOptionsMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.home_menu, menu)
                    orderMenuItem = menu.findItem(R.id.menu_order_by)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_filter -> {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToFilterPurchasesFragment()
                            findNavController().navigate(action)
                            true
                        }
                        R.id.action_order_time_asc -> {
                            viewModel.setOrder(OrderBy.TIME_ASC)
                            true
                        }
                        R.id.action_order_time_desc -> {
                            viewModel.setOrder(OrderBy.TIME_DESC)
                            true
                        }
                        R.id.action_order_price_asc -> {
                            viewModel.setOrder(OrderBy.PRICE_ASC)
                            true
                        }
                        R.id.action_order_price_desc -> {
                            viewModel.setOrder(OrderBy.PRICE_DESC)
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.STARTED
        )
    }
}