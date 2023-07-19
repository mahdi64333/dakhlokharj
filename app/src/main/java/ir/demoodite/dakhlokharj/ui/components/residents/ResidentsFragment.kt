package ir.demoodite.dakhlokharj.ui.components.residents

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.FragmentResidentsBinding
import ir.demoodite.dakhlokharj.models.AsyncOperationStatus
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ResidentsFragment :
    BaseFragment<FragmentResidentsBinding>(FragmentResidentsBinding::inflate) {
    private val viewModel: ResidentsViewModel by viewModels()
    private var snackBar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResidentSaveUi()
        setupResidentsRecyclerView()
    }

    override fun onStop() {
        super.onStop()

        snackBar?.dismiss()
    }

    private fun setupResidentSaveUi() {
        binding.textInputLayoutResidentName.setEndIconOnClickListener {
            binding.textInputLayoutResidentName.setEndIconActivated(false)
            validateInputsAndGetResident()?.let {
                insertEditingResident(it)
            }
            binding.textInputLayoutResidentName.setEndIconActivated(true)
        }
    }

    private fun insertEditingResident(resident: Resident) {
        (binding.rvResidents.adapter as ResidentsListAdapter).endEditing()
        val insertionStatus = MutableSharedFlow<AsyncOperationStatus>()
        lifecycleScope.launch {
            insertionStatus.first {
                if (it.isSuccessful) {
                    binding.textInputEditTextResidentName.setText("")
                    UiUtil.hideKeyboard(binding.textInputEditTextResidentName)
                } else {
                    binding.textInputLayoutResidentName.error =
                        getString(R.string.duplicate)
                }
                true
            }
        }
        viewModel.insertResident(insertionStatus, resident)
    }

    private fun validateInputsAndGetResident(): Resident? {
        var errorFlag = false
        val resident = Resident(0, "", true)
        val name = binding.textInputEditTextResidentName.text.toString().trim()

        if (name.isEmpty()) {
            binding.textInputLayoutResidentName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnType(binding.textInputEditTextResidentName)
            errorFlag = true
        } else {
            resident.name = name
        }

        return if (errorFlag) null else resident
    }

    private fun setupResidentsRecyclerView() {
        binding.rvResidents.adapter = ResidentsListAdapter().apply {
            onActivationChangedListener = { resident, active ->
                resident.active = active
                val updateStatus = MutableSharedFlow<AsyncOperationStatus>()
                lifecycleScope.launch {
                    updateStatus.first {
                        if (!it.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                R.string.an_error_has_occurred,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                }
                viewModel.updateResident(updateStatus, resident)
            }
            onNameChangedListener = { resident, newName, editText, viewHolder ->
                resident.name = newName
                val updateStatus = MutableSharedFlow<AsyncOperationStatus>()
                lifecycleScope.launch {
                    updateStatus.first {
                        if (it.isSuccessful) {
                            UiUtil.hideKeyboard(editText)
                            viewHolder.setEditing(false)
                        } else {
                            val textInputLayout = editText.parent.parent as TextInputLayout
                            textInputLayout.error = getString(R.string.duplicate)
                            UiUtil.removeErrorOnType(editText)
                        }
                        true
                    }
                }
                viewModel.updateResident(updateStatus, resident)
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.residentsStateFlow.collectLatest {
                        submitList(it)
                        binding.rvResidents.isGone = it.isEmpty()
                        binding.tvNoData.isVisible = it.isEmpty()
                    }
                }
            }
        }

        binding.rvResidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResidents.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(),
                MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            }
        )

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val residentsAdapter = binding.rvResidents.adapter!! as ResidentsListAdapter
                val residents = LinkedList(residentsAdapter.currentList)
                val residentPosition = viewHolder.adapterPosition
                val resident = residents[residentPosition]
                residentsAdapter.endEditing()
                residents.removeAt(residentPosition)
                residentsAdapter.submitList(residents)
                snackBar?.dismiss()
                snackBar =
                    Snackbar.make(binding.root, R.string.resident_got_deleted, Snackbar.LENGTH_LONG)
                val snackBarCallback = object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)

                        viewModel.deleteResident(resident)
                    }
                }
                snackBar?.apply {
                    setAction(R.string.undo) {
                        residents.add(residentPosition, resident)
                        residentsAdapter.submitList(residents)
                        binding.rvResidents.adapter = residentsAdapter
                        removeCallback(snackBarCallback)
                        dismiss()
                    }
                    addCallback(snackBarCallback)
                    show()
                }
            }
        }).attachToRecyclerView(binding.rvResidents)
    }
}