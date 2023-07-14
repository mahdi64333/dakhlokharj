package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.*
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
import ir.demoodite.dakhlokharj.fragments.filters.*
import ir.demoodite.dakhlokharj.models.viewmodels.FilterPurchasesViewModel

@AndroidEntryPoint
class FilterPurchasesFragment : Fragment() {
    private var _binding: FragmentFilterPurchasesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FilterPurchasesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFilterPurchasesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupMenuProvider()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 5
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FilterProductNameFragment()
                    1 -> FilterPriceFragment()
                    2 -> FilterBuyerFragment()
                    3 -> FilterTimeFragment()
                    else -> FilterConsumerFragment()
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.product)
                1 -> tab.text = getString(R.string.price)
                2 -> tab.text = getString(R.string.buyer)
                3 -> tab.text = getString(R.string.time)
                4 -> tab.text = getString(R.string.consumer)
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
//                        TODO "Hide and show the delete menu icon"
//                        when (position) {
//                            0 -> return
//                            1 -> return
//                            2 -> return
//                            3 -> return
//                            4 -> return
//                        }
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        // TODO "Delete all filtered entries"
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }
}