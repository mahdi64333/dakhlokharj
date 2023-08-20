package ir.demoodite.dakhlokharj.ui.components.filterPurchases

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPurchasesBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters.*
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterPurchasesFragment :
    BaseFragment<FragmentFilterPurchasesBinding>(FragmentFilterPurchasesBinding::inflate) {
    /**
     * Shared [ViewModel] object to store data for all fragments inside ```filterPurchases```
     * package.
     */
    private val viewModel: FilterPurchasesViewModel by activityViewModels()

    /**
     * The menu item for deleting all filtered purchases visible to user.
     * It must become hidden when there is no filtered purchase.
     */
    private var deleteAllMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupMenuProvider()
    }

    override fun onStop() {
        super.onStop()

        /*
        * The menu item has to be set to null on fragment's stop.
        * To prevent access from menu item after options menu gets destroyed and also to prevent
        * memory leak.
        */
        deleteAllMenuItem = null
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.clearStateFlows()
    }

    private fun startFlowCollection() {
        // Listens for change of current filter purchases list to update "Delete all" menu item
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredPurchasesChangedChannel.collectLatest { filterType ->
                    updateDeleteAllMenuItemVisibility(filterType)
                }
            }
        }

        // Listens for purchase deletion
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deletionChannel.collectLatest { purchaseDeleteType ->
                    showPurchaseDeletionSnackbar(purchaseDeleteType)
                }
            }
        }
    }

    /**
     * Changes visibility of [deleteAllMenuItem] according to purchase list of [filterType].
     * The menu item will become invisible if the list is empty.
     *
     * @param filterType [deleteAllMenuItem] visibility will set according to
     * purchases, filtered by this filter
     */
    private fun updateDeleteAllMenuItemVisibility(filterType: PurchaseFilters) {
        deleteAllMenuItem?.isVisible =
            viewModel.getFilteredPurchasesStateFlow(filterType).value?.first?.isNotEmpty()
                ?: false
    }

    /**
     * Shows a [Snackbar] to notify user of deletion. This [Snackbar] has an "Undo" button
     * to undo deletion.
     */
    private fun showPurchaseDeletionSnackbar(purchaseDeleteType: FilterPurchasesViewModel.PurchaseDeleteType) {
        Snackbar.make(
            binding.root, getString(R.string.purchase_got_deleted), Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.undo) {
                when (purchaseDeleteType) {
                    FilterPurchasesViewModel.PurchaseDeleteType.SINGLE_PURCHASE ->
                        viewModel.undoPurchaseDelete()
                    FilterPurchasesViewModel.PurchaseDeleteType.MULTIPLE_PURCHASE ->
                        viewModel.undoBatchPurchaseDelete()
                }
            }
            show()
        }
    }

    /**
     * Setup [ViewPager2] for purchase filter fragments with a [TabLayout].
     */
    private fun setupViewPager() {
        // ViewPager adapter setup
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return PurchaseFilters.size
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    PurchaseFilters.PRODUCT_NAME.ordinal -> FilterProductNameFragment()
                    PurchaseFilters.PRICE.ordinal -> FilterPriceFragment()
                    PurchaseFilters.BUYER.ordinal -> FilterBuyerFragment()
                    PurchaseFilters.TIME.ordinal -> FilterTimeFragment()
                    PurchaseFilters.CONSUMER.ordinal -> FilterConsumerFragment()
                    else -> throw IllegalArgumentException()
                }
            }
        }

        // Listen to page change to update deleteAllMenuItem based on the name filter page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                updateDeleteAllMenuItemVisibility(PurchaseFilters[position])
            }
        })

        // TabLayout setup
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                PurchaseFilters.PRODUCT_NAME.ordinal -> tab.text = getString(R.string.product)
                PurchaseFilters.PRICE.ordinal -> tab.text = getString(R.string.price)
                PurchaseFilters.BUYER.ordinal -> tab.text = getString(R.string.buyer)
                PurchaseFilters.TIME.ordinal -> tab.text = getString(R.string.time)
                PurchaseFilters.CONSUMER.ordinal -> tab.text = getString(R.string.consumer)
            }
        }.attach()
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.filter_menu, menu)
                deleteAllMenuItem = menu.findItem(R.id.action_delete_all_filtered)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all_filtered -> {
                        showDeleteAllPurchasesDialog()

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun showDeleteAllPurchasesDialog() {
        UiUtil.setSweetAlertDialogNightMode(resources)
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE).apply {
            titleText = getString(R.string.are_you_sure_to_delete)
            confirmText = getString(R.string.yes)
            cancelText = getString(R.string.cancel)
            setConfirmClickListener {
                viewModel.requestBatchFilteredPurchasesDelete(PurchaseFilters[binding.viewPager.currentItem])
                dismiss()
            }
            show()
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CONFIRM))
            UiUtil.fixSweetAlertDialogButton(getButton(SweetAlertDialog.BUTTON_CANCEL))
        }
    }
}