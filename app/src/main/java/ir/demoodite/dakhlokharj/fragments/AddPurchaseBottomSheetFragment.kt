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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.adapters.ConsumersListAdapter
import ir.demoodite.dakhlokharj.databinding.FragmentAddPurchaseBinding
import ir.demoodite.dakhlokharj.models.database.Purchase
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.models.viewmodels.AddPurchaseViewModel
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate

@AndroidEntryPoint
class AddPurchaseBottomSheetFragment :
    BottomSheetDialogFragment() {
    private var _binding: FragmentAddPurchaseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPurchaseViewModel by activityViewModels()
    private lateinit var residents: List<Resident>
    private lateinit var activeResidents: List<Resident>
    private lateinit var selectedResidents: List<Resident>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsStateFlow.collectLatest {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        residents = it
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeResidentsStateFlow.collectLatest {
                    activeResidents = it
                    withStarted {
                        setupBuyerAutocompleteTextViewAdapter()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
        setupConsumerAutocompleteTextView()
        setupConsumerChips()
        binding.btnSubmit.setOnClickListener {
            it.isEnabled = false
            validateInputsAndGetPurchase()?.let { purchase ->
                viewModel.savePurchaseRecord(purchase, selectedResidents)
                cleanupAndDismiss()
            }
            it.isEnabled = true
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

    private fun setupBuyerAutocompleteTextViewAdapter() {
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

    private fun setupConsumerAutocompleteTextViewAdapter(unselectedResidentNames: List<String>) {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            unselectedResidentNames
        )
        binding.autoCompleteTextViewConsumerName.setAdapter(arrayAdapter)
    }

    private fun setupConsumerAutocompleteTextView() {
        binding.autoCompleteTextViewConsumerName.setOnClickListener {
            binding.autoCompleteTextViewConsumerName.showDropDown()
        }
        binding.textInputLayoutConsumerName.setEndIconOnClickListener {
            val textInputLayout = binding.textInputLayoutConsumerName
            val enteredName = binding.autoCompleteTextViewConsumerName.text.toString()

            if (enteredName.isEmpty()) {
                textInputLayout.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnType(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }
            if (residents.find { it.name == enteredName } == null) {
                textInputLayout.error = getString(R.string.no_residents_found)
                UiUtil.removeErrorOnType(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }
            if (activeResidents.find { it.name == enteredName } == null) {
                textInputLayout.error = getString(R.string.no_active_residents_found)
                UiUtil.removeErrorOnType(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }

            if (selectedResidents.find { it.name == enteredName } == null) {
                val selectedResident = activeResidents.find { it.name == enteredName }!!
                viewModel.addSelectedResident(selectedResident)
            }
            binding.autoCompleteTextViewConsumerName.setText("")
        }
        binding.textInputLayoutConsumerName.setEndIconOnLongClickListener {
            activeResidents.forEach { activeResident ->
                viewModel.addSelectedResident(activeResident)
            }
            true
        }
    }

    private fun setupConsumerChips() {
        val adapter = ConsumersListAdapter() {
            viewModel.removeSelectedResident(it)
        }
        binding.rvConsumers.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        binding.rvConsumers.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedResidentsStateFlow.collectLatest {
                    val residents = viewModel.selectedResidentsStateFlow.first()
                    selectedResidents = it.filter {
                        residents.contains(it)
                    }
                    val unselectedResidentNames = viewModel.activeResidentsStateFlow.first()
                        .filter {
                            !selectedResidents.contains(it)
                        }.map {
                            it.name
                        }
                    withStarted {
                        setupConsumerAutocompleteTextViewAdapter(unselectedResidentNames)
                        adapter.submitList(selectedResidents)
                    }

                }
            }
        }
    }

    private fun validateInputsAndGetPurchase(): Purchase? {
        var errorFlag = false
        val productName = binding.textInputEditTextProductName.text.toString().trim()
        val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
        val price = if (priceText.isEmpty()) 0 else priceText.toLong()
        val buyerName = binding.autoCompleteTextViewProductBuyer.text.toString().trim()
        val buyer = activeResidents.find { it.name == buyerName }

        if (productName.isEmpty()) {
            binding.textInputLayoutProductName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnType(binding.textInputEditTextProductName)
            errorFlag = true
        }
        if (priceText.isEmpty()) {
            binding.textInputLayoutProductPrice.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnType(binding.textInputEditTextProductPrice)
            errorFlag = true
        } else if (price <= 0) {
            binding.textInputLayoutProductPrice.error = getString(R.string.must_be_positive)
            UiUtil.removeErrorOnType(binding.textInputEditTextProductPrice)
            errorFlag = true
        }
        if (buyerName.isEmpty()) {
            binding.textInputLayoutProductBuyer.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnType(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!activeResidents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error =
                getString(R.string.no_active_residents_found)
            UiUtil.removeErrorOnType(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!residents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error =
                getString(R.string.no_residents_found)
            UiUtil.removeErrorOnType(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        }
        if (selectedResidents.isEmpty()) {
            binding.textInputLayoutConsumerName.error =
                getString(R.string.no_consumer_selected)
            UiUtil.removeErrorOnType(binding.autoCompleteTextViewConsumerName)
            errorFlag = true
        }

        return if (errorFlag) {
            null
        } else {
            Purchase(
                0,
                productName,
                price,
                buyer!!.id,
                PersianDate().time
            )
        }
    }

    private fun cleanupAndDismiss() {
        viewModel.clearSelectedResident()
        binding.apply {
            textInputEditTextProductName.setText("")
            textInputEditTextProductPrice.setText("")
            autoCompleteTextViewProductBuyer.setText("")
            autoCompleteTextViewConsumerName.setText("")
        }
        dismiss()
    }

    companion object {
        const val TAG = "AddPurchaseBottomSheetFragment"
    }
}