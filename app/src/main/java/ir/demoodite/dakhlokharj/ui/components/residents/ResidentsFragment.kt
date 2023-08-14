package ir.demoodite.dakhlokharj.ui.components.residents

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val viewModel: ResidentsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDataCollection()
        setupErrorReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupResidentSaveUi()
        setupResidentsRecyclerView()
        showShowcaseIfNotShown()
    }

    private fun startDataCollection() {
        lifecycleScope.launch {
            viewModel.residentsStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updateResidentsUi(it)
                }
            }
        }
    }

    private fun updateResidentsUi(residents: List<Resident>) {
        val residentListAdapter = binding.rvResidents.adapter as ResidentsListAdapter
        binding.tvNoData.isVisible = residents.isEmpty()
        residentListAdapter.submitList(residents)
    }

    private fun setupErrorReceiver() {
        lifecycleScope.launch {
            viewModel.residentNameInputErrorResChannel.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    binding.textInputLayoutResidentName.error = getString(R.string.duplicate)
                    UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName)
                }
            }
        }
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
        (binding.rvResidents.adapter as ResidentsListAdapter).stopEditing()
        viewModel.insertResident(resident)
        binding.textInputEditTextResidentName.setText("")
    }

    private fun validateInputsAndGetResident(): Resident? {
        var errorFlag = false
        val resident = Resident(0)
        val name = binding.textInputEditTextResidentName.text.toString().trim()

        if (name.isEmpty()) {
            binding.textInputLayoutResidentName.error = getString(R.string.its_empty)
            UiUtil.removeErrorOnTextChange(binding.textInputEditTextResidentName)
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
                viewModel.updateResident(resident)
            }
            onNameChangedListener = { resident, newName ->
                resident.name = newName
                viewModel.updateResident(resident)
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
                Log.d("fuck", adapter.currentList.toString())
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