package ir.demoodite.dakhlokharj.ui.components.filterpurchases

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPurchasesBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters.*
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterPurchasesFragment :
    BaseFragment<FragmentFilterPurchasesBinding>(FragmentFilterPurchasesBinding::inflate) {
    private val viewModel: FilterPurchasesViewModel by activityViewModels()
    private var deleteAllMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
        setupDeletionChannelReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupMenuProvider()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.clearStateFlows()
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deletionChannel.collectLatest { purchaseDeleteType ->
                    Snackbar.make(
                        binding.root, getString(R.string.purchase_got_deleted), Snackbar.LENGTH_LONG
                    ).apply {
                        setAction(R.string.undo) {
                            when (purchaseDeleteType) {
                                FilterPurchasesViewModel.PurchaseDeleteType.SINGLE_PURCHASE ->
                                    viewModel.undoPurchaseDelete()
                                FilterPurchasesViewModel.PurchaseDeleteType.MULTIPLE_PURCHASE ->
                                    viewModel.undoPurchasesDelete()
                            }
                        }
                        show()
                    }
                }
            }
        }
    }

    private fun setupDeletionChannelReceiver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredPurchasesChangedChannel.collectLatest {
                    updateDeleteAllMenuItemVisibility(it)
                }
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 5
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    FilterBy.PRODUCT_NAME.ordinal -> FilterProductNameFragment()
                    FilterBy.PRICE.ordinal -> FilterPriceFragment()
                    FilterBy.BUYER.ordinal -> FilterBuyerFragment()
                    FilterBy.TIME.ordinal -> FilterTimeFragment()
                    FilterBy.CONSUMER.ordinal -> FilterConsumerFragment()
                    else -> throw IllegalArgumentException()
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                FilterBy.PRODUCT_NAME.ordinal -> tab.text = getString(R.string.product)
                FilterBy.PRICE.ordinal -> tab.text = getString(R.string.price)
                FilterBy.BUYER.ordinal -> tab.text = getString(R.string.buyer)
                FilterBy.TIME.ordinal -> tab.text = getString(R.string.time)
                FilterBy.CONSUMER.ordinal -> tab.text = getString(R.string.consumer)
            }
        }.attach()
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.filter_menu, menu)
                deleteAllMenuItem = menu.findItem(R.id.action_delete_all_filtered)

                binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        updateDeleteAllMenuItemVisibility(FilterBy[position])
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all_filtered -> {
                        UiUtil.setSweetAlertDialogNightMode(resources)
                        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE).apply {
                            titleText = getString(R.string.are_you_sure_to_delete)
                            confirmText = getString(R.string.yes)
                            cancelText = getString(R.string.cancel)
                            setConfirmClickListener {
                                val currentFilter = FilterBy[binding.viewPager.currentItem]
                                val purchases =
                                    viewModel.getFilteredPurchasesStateFlow(currentFilter).value?.first?.map {
                                        it.purchase
                                    } ?: listOf()
                                viewModel.requestPurchasesDelete(purchases)
                                dismiss()
                            }
                            show()
                            UiUtil.fixSweetAlertDialogButtons(getButton(SweetAlertDialog.BUTTON_CONFIRM))
                            UiUtil.fixSweetAlertDialogButtons(getButton(SweetAlertDialog.BUTTON_CANCEL))
                        }

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun updateDeleteAllMenuItemVisibility(filterType: FilterBy) {
        deleteAllMenuItem?.isVisible =
            viewModel.getFilteredPurchasesStateFlow(filterType).value?.first?.isNotEmpty()
                ?: false
    }
}