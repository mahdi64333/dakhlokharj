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
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.FragmentFilterPurchasesBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters.*

@AndroidEntryPoint
class FilterPurchasesFragment :
    BaseFragment<FragmentFilterPurchasesBinding>(FragmentFilterPurchasesBinding::inflate) {
    private val viewModel: FilterPurchasesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupMenuProvider()
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

                binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val deleteAllMenuItem = menu.findItem(R.id.action_delete_all_filtered)
                        deleteAllMenuItem.isVisible =
                            viewModel.getPurchasesStateFlow(FilterBy[position]).value?.first?.isEmpty()
                                ?: false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all_filtered -> {
                        // TODO "Delete all filtered entries"
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }
}