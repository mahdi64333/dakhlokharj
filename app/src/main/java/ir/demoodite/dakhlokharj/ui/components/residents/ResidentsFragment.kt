package ir.demoodite.dakhlokharj.ui.components.residents

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.databinding.FragmentResidentsBinding
import ir.demoodite.dakhlokharj.databinding.ItemResidentBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.showcase.ShowcaseStatus
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType
import java.util.*

@AndroidEntryPoint
class ResidentsFragment :
    BaseFragment<FragmentResidentsBinding>(FragmentResidentsBinding::inflate) {
    private fun RecyclerView.Adapter<ViewHolder>.asResidentsListAdapter(): ResidentsListAdapter =
        this as ResidentsListAdapter

    private val viewModel: ResidentsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
        startResidentOperationChannelCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResidentSaveUi()
        setupResidentsRecyclerView()
        overrideBackPressedWhenEditing()
        showShowcaseIfNotShown()
    }

    private fun startFlowCollection() {
        // Residents collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentsStateFlow.collectLatest { newResidents ->
                    updateResidentsUi(newResidents)
                }
            }
        }
    }

    private fun updateResidentsUi(residents: List<Resident>) {
        binding.tvNoData.isVisible = residents.isEmpty()
        binding.rvResidents.adapter?.asResidentsListAdapter()?.submitList(residents)
    }

    private fun startResidentOperationChannelCollection() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.residentOperationChannel.collectLatest { residentOperationResult ->
                    handleResidentOperationResult(residentOperationResult)
                }
            }
        }
    }

    private fun handleResidentOperationResult(
        residentOperationResult: ResidentsViewModel.ResidentOperationResult,
    ) {
        when (residentOperationResult.operationType) {
            ResidentsViewModel.ResidentOperationResult.ResidentOperationType.INSERT -> {
                if (residentOperationResult.isSuccessful) {
                    binding.textInputEditTextResidentName.setText("")
                } else {
                    residentOperationResult.messageStringRes?.let { errorStringRes ->
                        binding.textInputLayoutResidentName.error = getString(errorStringRes)
                        UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName)
                    }
                }
            }
            ResidentsViewModel.ResidentOperationResult.ResidentOperationType.UPDATE -> {
                if (residentOperationResult.isSuccessful) {
                    binding.rvResidents.adapter?.asResidentsListAdapter()?.stopEditing()
                } else {
                    residentOperationResult.messageStringRes?.let { errorStringRes ->
                        binding.rvResidents.adapter?.asResidentsListAdapter()
                            ?.setError(getString(errorStringRes))
                    }
                }
            }
        }
    }

    private fun setupResidentSaveUi() {
        binding.textInputLayoutResidentName.setEndIconOnClickListener {
            binding.rvResidents.adapter?.asResidentsListAdapter()?.stopEditing()
            validateInputsAndGetResidentOrNull()?.let { newResident ->
                viewModel.insertResident(newResident)
            }
        }

    }

    private fun validateInputsAndGetResidentOrNull(): Resident? {
        var errorFlag = false
        val name = binding.textInputEditTextResidentName.text.toString().trim()

        if (name.isEmpty()) {
            binding.textInputLayoutResidentName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName)
            errorFlag = true
        }

        return if (errorFlag) null
        else Resident(name = name)
    }

    private fun setupResidentsRecyclerView() {
        binding.rvResidents.adapter = ResidentsListAdapter().apply {
            onActivationChangedListener = { resident, active ->
                resident.active = active
                viewModel.updateResident(resident)
            }
            onNameChangedListener = { resident, newName ->
                viewModel.updateResident(resident.copy(name = newName))
            }
        }

        binding.rvResidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResidents.addItemDecoration(MaterialDividerItemDecoration(
            requireContext(), MaterialDividerItemDecoration.VERTICAL
        ).apply {
            isLastItemDecorated = false
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val residentsAdapter = binding.rvResidents.adapter!! as ResidentsListAdapter
                val residents = LinkedList(residentsAdapter.currentList)
                val residentPosition = viewHolder.adapterPosition
                val resident = residents[residentPosition]
                residentsAdapter.stopEditing()
                residents.removeAt(residentPosition)
                residentsAdapter.submitList(residents)

                Snackbar.make(binding.root, R.string.resident_got_deleted, Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(R.string.undo) {
                            residents.add(residentPosition, resident)
                            residentsAdapter.submitList(residents)
                            binding.rvResidents.adapter = residentsAdapter
                        }
                        addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int,
                            ) {
                                super.onDismissed(transientBottomBar, event)

                                if (event != DISMISS_EVENT_ACTION) {
                                    viewModel.deleteResident(resident)
                                }
                            }
                        })
                        show()
                    }
            }
        }).attachToRecyclerView(binding.rvResidents)
    }

    private fun overrideBackPressedWhenEditing() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.rvResidents.adapter?.asResidentsListAdapter()?.isEditing() == true) {
                binding.rvResidents.adapter!!.asResidentsListAdapter().stopEditing()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun showShowcaseIfNotShown() {
        val showcaseStatus = ShowcaseStatus(requireContext())

        if (showcaseStatus.isShowcaseShown(ShowcaseStatus.Screen.RESIDENTS)) {
            return
        }

        val adapter = binding.rvResidents.adapter as ResidentsListAdapter

        if (viewModel.residentsStateFlow.value.isEmpty()) {
            lifecycleScope.launch {
                delay(50)
                binding.rvResidents.itemAnimator = null
                if (viewModel.residentsStateFlow.value.isEmpty()) {
                    adapter.submitList(
                        listOf(
                            Resident(
                                name = getString(R.string.showcase_resident)
                            )
                        )
                    )
                    binding.tvNoData.isGone = true
                }
            }
        }

        GuideView.Builder(requireActivity())
            .setContentText(getString(R.string.showcase_resident_input))
            .setDismissType(DismissType.anywhere).setTargetView(binding.layoutResidentName)
            .setPointerType(PointerType.arrow)
            .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
            .setContentTextSize(15).setGuideListener {
                val itemResidentBinding = ItemResidentBinding.bind(binding.rvResidents[0])
                itemResidentBinding.textInputEditTextResidentName.performLongClick()
                UiUtil.hideKeyboard(itemResidentBinding.textInputEditTextResidentName)

                val residentSwipeShowcase = GuideView.Builder(requireActivity())
                    .setContentText(getString(R.string.showcase_delete_resident))
                    .setDismissType(DismissType.anywhere).setTargetView(binding.rvResidents[0])
                    .setPointerType(PointerType.arrow)
                    .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
                    .setContentTextSize(15).setGuideListener {
                        if (viewModel.residentsStateFlow.value.isEmpty()) {
                            adapter.submitList(emptyList()) {
                                binding.rvResidents.itemAnimator = DefaultItemAnimator()
                                binding.tvNoData.isGone = false
                            }
                        } else {
                            binding.rvResidents[0].clearAnimation()
                        }
                        showcaseStatus.recordShowcaseEnd(ShowcaseStatus.Screen.RESIDENTS)
                    }.build()

                val residentActivationShowcase = GuideView.Builder(requireActivity())
                    .setContentText(getString(R.string.showcase_active_resident))
                    .setDismissType(DismissType.anywhere)
                    .setTargetView(itemResidentBinding.checkBoxActive)
                    .setPointerType(PointerType.arrow)
                    .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
                    .setContentTextSize(15).setGuideListener {
                        val swipeAnimation =
                            AnimationUtils.loadAnimation(requireContext(), R.anim.move_to_side)
                        swipeAnimation.isFillEnabled = true
                        swipeAnimation.fillAfter = true
                        binding.rvResidents[0].startAnimation(swipeAnimation)
                        residentSwipeShowcase.show()
                    }.build()

                GuideView.Builder(requireActivity())
                    .setContentText(getString(R.string.showcase_resident_name_edit))
                    .setDismissType(DismissType.anywhere).setTargetView(binding.rvResidents[0])
                    .setPointerType(PointerType.arrow)
                    .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
                    .setContentTextSize(15).setGuideListener {
                        adapter.stopEditing()
                        residentActivationShowcase.show()
                    }.build().show()
            }.build().show()
    }
}