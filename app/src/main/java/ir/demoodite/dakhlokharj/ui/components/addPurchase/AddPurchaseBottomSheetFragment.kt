package ir.demoodite.dakhlokharj.ui.components.addPurchase

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.FragmentAddPurchaseBinding
import ir.demoodite.dakhlokharj.ui.base.BaseBottomSheetDialogFragment
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate

@AndroidEntryPoint
class AddPurchaseBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentAddPurchaseBinding>(FragmentAddPurchaseBinding::inflate) {
    private fun RecyclerView.Adapter<ViewHolder>.asSelectedConsumersListAdapter(): SelectedConsumersListAdapter =
        this as SelectedConsumersListAdapter

    private val viewModel: AddPurchaseViewModel by activityViewModels()
    private lateinit var residents: List<Resident>
    private lateinit var activeResidents: List<Resident>
    private lateinit var selectedResidents: List<Resident>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInputsWithLastEnteredPurchaseInfo()
        setupConsumerAutocompleteTextView()
        setupConsumerChips()
        setupSubmitButton()
    }

    override fun onDestroyView() {
        saveCurrentInputData()

        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // Setting the peek height at full window height
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            bottomSheetDialog.behavior.peekHeight = resources.displayMetrics.heightPixels
        }

        return dialog
    }

    private fun startFlowCollection() {
        // All residents collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsStateFlow.collectLatest { newResidents ->
                    residents = newResidents
                }
            }
        }

        // Active residents collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeResidentsStateFlow.collectLatest { newActiveResidents ->
                    updateBuyerAutocompleteTextViewAdapter(newActiveResidents)
                }
            }
        }

        // Selected residents collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedResidentsStateFlow.collectLatest { newSelectedResidents ->
                    // Updating consumers RecyclerView
                    val selectedResidents = residents.intersect(newSelectedResidents).toList()
                    updateSelectedResidentsRecyclerView(selectedResidents)

                    // Updating consumers AutocompleteTextView
                    val unselectedResidentNames =
                        residents.subtract(selectedResidents.toSet()).map { resident ->
                            resident.name
                        }
                    updateConsumerAutocompleteTextViewAdapter(unselectedResidentNames)
                }
            }
        }
    }

    private fun updateBuyerAutocompleteTextViewAdapter(activeResidents: List<Resident>) {
        val activeResidentNames = activeResidents.map {
            it.name
        }
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, activeResidentNames
        )
        binding.autoCompleteTextViewProductBuyer.setAdapter(arrayAdapter)
    }

    private fun updateSelectedResidentsRecyclerView(selectedResidents: List<Resident>) {
        binding.tvLabelChips.isVisible = selectedResidents.isNotEmpty()
        binding.rvConsumers.adapter?.asSelectedConsumersListAdapter()?.submitList(selectedResidents)
    }

    private fun updateConsumerAutocompleteTextViewAdapter(unselectedResidentNames: List<String>) {
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, unselectedResidentNames
        )
        binding.autoCompleteTextViewConsumerName.setAdapter(arrayAdapter)
    }

    private fun fillInputsWithLastEnteredPurchaseInfo() {
        viewModel.savedPurchaseInfo.let { purchase ->
            binding.textInputEditTextProductName.setText(purchase.product)
            binding.textInputEditTextProductPrice.setText(if (purchase.price > 0) purchase.price.toString() else "")
            val buyer = activeResidents.find { resident ->
                resident.id == purchase.buyerId
            }
            binding.autoCompleteTextViewProductBuyer.setText(buyer?.name)
        }
    }

    private fun setupConsumerAutocompleteTextView() {
        binding.autoCompleteTextViewConsumerName.setOnClickListener {
            binding.autoCompleteTextViewConsumerName.showDropDown()
        }
        binding.textInputLayoutConsumerName.setEndIconOnClickListener {
            validateAndAddConsumer()
        }
        binding.textInputLayoutConsumerName.setEndIconOnLongClickListener {
            binding.autoCompleteTextViewConsumerName.setText("")
            activeResidents.forEach { activeResident ->
                viewModel.addSelectedResident(activeResident)
            }
            true
        }
    }

    private fun validateAndAddConsumer() {
        val enteredName = binding.autoCompleteTextViewConsumerName.text.toString()

        if (enteredName.isEmpty()) {
            binding.textInputLayoutConsumerName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
            return
        }
        if (residents.find { it.name == enteredName } == null) {
            binding.textInputLayoutConsumerName.error = getString(R.string.no_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
            return
        }
        if (activeResidents.find { it.name == enteredName } == null) {
            binding.textInputLayoutConsumerName.error =
                getString(R.string.no_active_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
            return
        }

        if (selectedResidents.find { it.name == enteredName } == null) {
            val selectedResident = activeResidents.find { it.name == enteredName }!!
            viewModel.addSelectedResident(selectedResident)
        }
        binding.autoCompleteTextViewConsumerName.setText("")
    }

    private fun setupConsumerChips() {
        binding.rvConsumers.adapter = SelectedConsumersListAdapter {
            viewModel.removeSelectedResident(it)
        }
        binding.rvConsumers.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            it.isEnabled = false
            validateInputsAndGetPurchaseOrNull()?.let { purchase ->
                viewModel.savePurchase(
                    purchase, selectedResidents.toList()
                )
                cleanupInputs()
                dismiss()
            }
            it.isEnabled = true
        }
    }

    private fun validateInputsAndGetPurchaseOrNull(): Purchase? {
        var errorFlag = false
        val productName = binding.textInputEditTextProductName.text.toString().trim()
        val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
        val price: Double = if (priceText.isEmpty()) 0.0 else priceText.toDouble()
        val buyerName = binding.autoCompleteTextViewProductBuyer.text.toString().trim()
        val buyer = activeResidents.find { it.name == buyerName }

        if (productName.isEmpty()) {
            binding.textInputLayoutProductName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductName)
            errorFlag = true
        }

        if (priceText.isEmpty()) {
            binding.textInputLayoutProductPrice.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductPrice)
            errorFlag = true
        } else if (price <= 0.0) {
            binding.textInputLayoutProductPrice.error = getString(R.string.must_be_positive)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductPrice)
            errorFlag = true
        }

        if (buyerName.isEmpty()) {
            binding.textInputLayoutProductBuyer.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!activeResidents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error =
                getString(R.string.no_active_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!residents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error = getString(R.string.no_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        }

        if (selectedResidents.isEmpty()) {
            binding.textInputLayoutConsumerName.error = getString(R.string.no_consumer_selected)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
            errorFlag = true
        }

        return if (errorFlag) {
            null
        } else {
            Purchase(
                0, productName, price, buyer!!.id, PersianDate().time
            )
        }
    }

    private fun cleanupInputs() {
        viewModel.clearSelectedResidents()
        binding.apply {
            textInputEditTextProductName.setText("")
            textInputEditTextProductPrice.setText("")
            autoCompleteTextViewProductBuyer.setText("")
            autoCompleteTextViewConsumerName.setText("")
        }
    }

    private fun saveCurrentInputData() {
        viewModel.savedPurchaseInfo.apply {
            product = binding.textInputEditTextProductName.text.toString()
            val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
            price = if (priceText.isNotEmpty()) priceText.toDouble() else -1.0
            val buyer =
                activeResidents.find { it.name == binding.autoCompleteTextViewProductBuyer.text.toString() }
            buyerId = buyer?.id ?: -1
        }
    }

    companion object {
        const val TAG = "AddPurchaseBottomSheetFragment"
    }
}