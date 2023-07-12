package ir.demoodite.dakhlokharj.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.adapters.ResidentsListAdapter
import ir.demoodite.dakhlokharj.databinding.FragmentResidentsBinding
import ir.demoodite.dakhlokharj.models.AsyncOperationStatus
import ir.demoodite.dakhlokharj.models.database.Resident
import ir.demoodite.dakhlokharj.models.viewmodels.ResidentsViewModel
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ResidentsFragment : Fragment() {
    private var _binding: FragmentResidentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResidentsViewModel by viewModels()
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResidentSaveUi()
        setupResidentsRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupResidentSaveUi() {
        binding.textInputLayoutResidentName.setEndIconOnClickListener {
            binding.textInputLayoutResidentName.setEndIconActivated(false)
            if (validateAndSetEditingResident()) {
                insertEditingResident()
            }
            binding.textInputLayoutResidentName.setEndIconActivated(true)
        }
    }

    private fun insertEditingResident() {
        val insertionStatus = MutableSharedFlow<AsyncOperationStatus>()
        lifecycleScope.launch {
            insertionStatus.first {
                if (!it.isSuccessful) {
                    binding.textInputLayoutResidentName.error =
                        getString(R.string.duplicate)
                    UiUtil.removeErrorOnType(binding.textInputEditTextResidentName)
                }
                true
            }
        }
        viewModel.insertEditingResident(insertionStatus)
    }

    private fun validateAndSetEditingResident(): Boolean {
        var errorFlag = false
        viewModel.editingResident = Resident(0, "", true)
        val name = binding.textInputEditTextResidentName.text.toString().trim()
        if (name.isEmpty()) {
            binding.textInputLayoutResidentName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnType(binding.textInputEditTextResidentName)
            errorFlag = true
        } else {
            viewModel.editingResident.name = name
        }

        return !errorFlag
    }

    private fun setupResidentsRecyclerView() {
        binding.rvResidents.adapter = ResidentsListAdapter().apply {
            onActivationChangedListener = { resident, active ->
                resident.active = active
                viewModel.editingResident = resident
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
                viewModel.updateEditingResident(updateStatus)
            }
            onNameChangedListener = { resident, newName, editText, viewHolder ->
                resident.name = newName
                viewModel.editingResident = resident
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
                viewModel.updateEditingResident(updateStatus)
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
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.HORIZONTAL
            )
        )

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val residentsAdapter = binding.rvResidents.adapter!! as ResidentsListAdapter
                val residents = LinkedList(residentsAdapter.currentList)
                val residentPosition = viewHolder.adapterPosition
                val resident = residents[residentPosition]
                residents.removeAt(residentPosition)
                residentsAdapter.submitList(residents)
                snackbar?.dismiss()
                snackbar =
                    Snackbar.make(binding.root, R.string.resident_got_deleted, Snackbar.LENGTH_LONG)
                val snackBarCallback = object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)

                        viewModel.deleteResident(resident)
                    }
                }
                snackbar?.apply {
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