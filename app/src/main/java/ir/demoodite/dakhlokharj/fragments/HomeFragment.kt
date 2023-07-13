package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.adapters.DetailedPurchasesListAdapter
import ir.demoodite.dakhlokharj.databinding.FragmentHomeBinding
import ir.demoodite.dakhlokharj.models.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
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
            // TODO "Show consumers names"
        }
        binding.rvPurchases.adapter = adapter
        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchases.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(),
                MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            }
        )
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.purchasesStateFlow.collectLatest {
                adapter.submitList(it)
            }
        }
    }
}