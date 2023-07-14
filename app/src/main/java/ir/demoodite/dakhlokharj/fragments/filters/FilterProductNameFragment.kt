package ir.demoodite.dakhlokharj.fragments.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentFilterProductNameBinding
import ir.demoodite.dakhlokharj.models.viewmodels.FilterPurchasesViewModel

@AndroidEntryPoint
class FilterProductNameFragment : Fragment() {
    private var _binding: FragmentFilterProductNameBinding? = null
    private val binding get() = _binding!!
    private val filteringViewModel: FilterPurchasesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFilterProductNameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}