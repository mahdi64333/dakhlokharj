package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentResidentsBinding
import ir.demoodite.dakhlokharj.models.viewmodels.ResidentsViewModel

@AndroidEntryPoint
class ResidentsFragment : Fragment() {
    private var _binding: FragmentResidentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResidentsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}