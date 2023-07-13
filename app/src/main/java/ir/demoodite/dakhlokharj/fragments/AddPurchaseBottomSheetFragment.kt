package ir.demoodite.dakhlokharj.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.databinding.FragmentAddPurchaseBinding
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.models.viewmodels.AddPurchaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddPurchaseBottomSheetFragment :
    BottomSheetDialogFragment() {
    private var _binding: FragmentAddPurchaseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPurchaseViewModel by activityViewModels()
    private lateinit var activeResidents: List<Resident>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeResidentsStateFlow.collectLatest {
                    activeResidents = it
                    launch {
                        withStarted {
                            setupBuyerAutocompleteTextView(activeResidents)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPurchaseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.savedPurchaseInfo.apply {
            binding.textInputEditTextProductName.setText(product)
            binding.textInputEditTextProductPrice.setText(if (price >= 0) price.toString() else "")
            val buyer =
                viewModel.activeResidentsStateFlow.value.find { it.id == buyerId }
            binding.autoCompleteTextViewProductBuyer.setText(buyer?.name)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.savedPurchaseInfo.apply {
            product = binding.textInputEditTextProductName.text.toString()
            val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
            price = if (priceText.isNotEmpty()) priceText.toLong() else -1
            val buyer =
                viewModel.activeResidentsStateFlow.value.find { it.name == binding.autoCompleteTextViewProductBuyer.text.toString() }
            buyerId = buyer?.id ?: -1
        }
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            bottomSheetDialog.behavior.peekHeight = resources.displayMetrics.heightPixels
        }
        return dialog
    }

    private fun setupBuyerAutocompleteTextView(activeResidents: List<Resident>) {
        val activeResidentNames = activeResidents.map {
            it.name
        }
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            activeResidentNames
        )
        binding.autoCompleteTextViewProductBuyer.setAdapter(arrayAdapter)
    }

    companion object {
        const val TAG = "AddPurchaseBottomSheetFragment"
    }
}