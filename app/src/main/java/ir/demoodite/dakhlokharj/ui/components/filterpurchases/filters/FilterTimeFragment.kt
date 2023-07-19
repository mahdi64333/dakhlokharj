package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentFilterTimeBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.FilterPurchasesViewModel

@AndroidEntryPoint
class FilterTimeFragment :
    BaseFragment<FragmentFilterTimeBinding>(FragmentFilterTimeBinding::inflate) {
    private val filteringViewModel: FilterPurchasesViewModel by activityViewModels()
}