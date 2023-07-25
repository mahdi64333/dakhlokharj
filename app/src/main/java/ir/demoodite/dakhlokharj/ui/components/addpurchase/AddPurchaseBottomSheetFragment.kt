package ir.demoodite.dakhlokharj.ui.components.addpurchase

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
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.FragmentAddPurchaseBinding
import ir.demoodite.dakhlokharj.ui.base.BaseBottomSheetDialogFragment
import ir.demoodite.dakhlokharj.ui.components.home.SelectedConsumersListAdapter
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate

@AndroidEntryPoint
class AddPurchaseBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentAddPurchaseBinding>(FragmentAddPurchaseBinding::inflate) {
    private val viewModel: AddPurchaseViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInputsWithLastEnteredPurchaseInfo()
        setupConsumerAutocompleteTextView()
        setupConsumerChips()
        setupSubmitButton()
    }

    override fun onDestroyView() {
        viewModel.savedPurchaseInfo.apply {
            product = binding.textInputEditTextProductName.text.toString()
            val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
            price = if (priceText.isNotEmpty()) priceText.toLong() else -1
            val buyer =
                viewModel.activeResidents.find { it.name == binding.autoCompleteTextViewProductBuyer.text.toString() }
            buyerId = buyer?.id ?: -1
        }

        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            bottomSheetDialog.behavior.peekHeight = resources.displayMetrics.heightPixels
        }
        return dialog
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.activeResidentsStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    setupBuyerAutocompleteTextViewAdapter()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedResidentsStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val selectedResidents = viewModel.residents.intersect(it).toList()
                    binding.tvLabelChips.isVisible = selectedResidents.isNotEmpty()
                    updateSelectedResidentsRecyclerView(selectedResidents)
                    val unselectedResidentNames = viewModel.residents.subtract(it).map { resident ->
                        resident.name
                    }
                    setupConsumerAutocompleteTextViewAdapter(unselectedResidentNames)
                }
            }
        }
    }

    private fun updateSelectedResidentsRecyclerView(selectedResidents: List<Resident>) {
        val selectedResidentsListAdapter =
            binding.rvConsumers.adapter as SelectedConsumersListAdapter
        selectedResidentsListAdapter.submitList(selectedResidents)
    }

    private fun setupBuyerAutocompleteTextViewAdapter() {
        val activeResidentNames = viewModel.activeResidents.map {
            it.name
        }
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, activeResidentNames
        )
        binding.autoCompleteTextViewProductBuyer.setAdapter(arrayAdapter)
    }

    private fun setupConsumerAutocompleteTextViewAdapter(unselectedResidentNames: List<String>) {
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, unselectedResidentNames
        )
        binding.autoCompleteTextViewConsumerName.setAdapter(arrayAdapter)
    }

    private fun fillInputsWithLastEnteredPurchaseInfo() {
        viewModel.savedPurchaseInfo.let { purchase ->
            binding.textInputEditTextProductName.setText(purchase.product)
            binding.textInputEditTextProductPrice.setText(if (purchase.price > 0) purchase.price.toString() else "")
            val buyer = viewModel.activeResidents.find { resident ->
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
            val textInputLayout = binding.textInputLayoutConsumerName
            val enteredName = binding.autoCompleteTextViewConsumerName.text.toString()

            if (enteredName.isEmpty()) {
                textInputLayout.error = getString(R.string.its_empty)
                UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }
            if (viewModel.residents.find { it.name == enteredName } == null) {
                textInputLayout.error = getString(R.string.no_residents_found)
                UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }
            if (viewModel.activeResidents.find { it.name == enteredName } == null) {
                textInputLayout.error = getString(R.string.no_active_residents_found)
                UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewConsumerName)
                return@setEndIconOnClickListener
            }

            if (viewModel.selectedResidents.find { it.name == enteredName } == null) {
                val selectedResident = viewModel.activeResidents.find { it.name == enteredName }!!
                viewModel.addSelectedResident(selectedResident)
            }
            binding.autoCompleteTextViewConsumerName.setText("")
        }
        binding.textInputLayoutConsumerName.setEndIconOnLongClickListener {
            binding.autoCompleteTextViewConsumerName.setText("")
            viewModel.activeResidents.forEach { activeResident ->
                viewModel.addSelectedResident(activeResident)
            }
            true
        }
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
            validateInputsAndGetPurchase()?.let { purchase ->
                viewModel.savePurchaseRecord(
                    purchase, viewModel.selectedResidents.toList()
                )
                cleanupAndDismiss()
            }
            it.isEnabled = true
        }
    }

    private fun validateInputsAndGetPurchase(): Purchase? {
        var errorFlag = false
        val productName = binding.textInputEditTextProductName.text.toString().trim()
        val priceText = binding.textInputEditTextProductPrice.getTextWithoutCommas()
        val price = if (priceText.isEmpty()) 0 else priceText.toLong()
        val buyerName = binding.autoCompleteTextViewProductBuyer.text.toString().trim()
        val buyer = viewModel.activeResidents.find { it.name == buyerName }

        if (productName.isEmpty()) {
            binding.textInputLayoutProductName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductName)
            errorFlag = true
        }
        if (priceText.isEmpty()) {
            binding.textInputLayoutProductPrice.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductPrice)
            errorFlag = true
        } else if (price <= 0) {
            binding.textInputLayoutProductPrice.error = getString(R.string.must_be_positive)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextProductPrice)
            errorFlag = true
        }
        if (buyerName.isEmpty()) {
            binding.textInputLayoutProductBuyer.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!viewModel.activeResidents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error =
                getString(R.string.no_active_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        } else if (!viewModel.residents.contains(buyer)) {
            binding.textInputLayoutProductBuyer.error = getString(R.string.no_residents_found)
            UiUtil.removeErrorOnTextChange(binding.autoCompleteTextViewProductBuyer)
            errorFlag = true
        }
        if (viewModel.selectedResidents.isEmpty()) {
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