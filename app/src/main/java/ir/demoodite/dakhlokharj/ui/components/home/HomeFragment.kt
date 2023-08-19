package ir.demoodite.dakhlokharj.ui.components.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
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
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.settings.enums.PurchasesOrderBy
import ir.demoodite.dakhlokharj.databinding.FragmentHomeBinding
import ir.demoodite.dakhlokharj.ui.base.BaseFragment
import ir.demoodite.dakhlokharj.ui.components.addPurchase.AddPurchaseBottomSheetFragment
import ir.demoodite.dakhlokharj.ui.showcase.ShowcaseStatus
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType
import java.util.*

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private fun RecyclerView.Adapter<ViewHolder>.asPurchasesListAdapter(): PurchasesListAdapter =
        this as PurchasesListAdapter

    private val viewModel: HomeViewModel by viewModels()

    /**
     * The menu item for selecting order of purchases in the home fragment.
     * It's icon must be updated when changing the setting.
     */
    private var purchaseListOrderMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startFlowCollection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLanguageSelectionFragmentOnFirstApplicationLaunch()
        setupPurchasesRecyclerView()
        setupFab()
        setupOptionsMenu()
    }

    override fun onStop() {
        super.onStop()

        /*
        * Purchase list order selection menu item has to be set to null on fragment's stop.
        * Because fragment's options menu gets destroyed when stopping the fragment.
        * */
        purchaseListOrderMenuItem = null
    }

    private fun startFlowCollection() {
        // Purchase list collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchasesStateFlow.collectLatest { newPurchases ->
                    updatePurchasesUi(newPurchases)
                }
            }
        }

        // Purchase list order collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchasesOrderStateFlow.collectLatest { newPurchaseListOrder ->
                    updateOrderMenuItemIcon(PurchasesOrderBy.valueOf(newPurchaseListOrder))
                }
            }
        }

        // Home showcase signal receiver
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                getShowcaseFeedbackReceiver().collectLatest { showcaseFeedbackType ->
                    when (showcaseFeedbackType) {
                        ShowcaseFeedbackType.START_HOME_SHOWCASE -> showShowcase()
                    }
                }
            }
        }
    }

    private fun updatePurchasesUi(detailedPurchases: List<DetailedPurchase>) {
        binding.rvPurchases.adapter?.asPurchasesListAdapter()?.submitList(detailedPurchases)
        binding.tvNoData.isVisible = detailedPurchases.isEmpty()
    }

    private fun updateOrderMenuItemIcon(purchasesOrderBy: PurchasesOrderBy) {
        purchaseListOrderMenuItem?.let { menuItem ->
            when (purchasesOrderBy) {
                PurchasesOrderBy.TIME_ASC -> menuItem.icon = ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_order_time_asc, requireContext().theme
                )
                PurchasesOrderBy.TIME_DESC -> menuItem.icon = ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_order_time_desc, requireContext().theme
                )
                PurchasesOrderBy.PRICE_ASC -> menuItem.icon = ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_order_price_asc, requireContext().theme
                )
                PurchasesOrderBy.PRICE_DESC -> menuItem.icon = ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_order_price_desc, requireContext().theme
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showShowcase() {
        val showcaseStatus = ShowcaseStatus(requireContext())

        // Adding a temporary showcase purchase item if there is no purchase in database
        if (viewModel.purchasesStateFlow.value.isEmpty()) {
            binding.rvPurchases.itemAnimator = null
            binding.rvPurchases.adapter?.asPurchasesListAdapter()?.submitList(
                listOf(
                    DetailedPurchase(
                        purchaseId = 0,
                        purchaseProduct = getString(R.string.showcase_product),
                        purchasePrice = getString(R.string.showcase_price).toDouble(),
                        purchaseTime = PersianDate().time,
                        purchaseBuyerId = 0,
                        buyerName = getString(R.string.showcase_resident)
                    )
                )
            )
            binding.tvNoData.isGone = true
        }

        // Starting the showcase
        /*
        * This code is a bit messy because of Android's recyclerView implementation.
        * A recyclerView item gets inserted with a delay and when adding a demonstrative
        * purchase item to the recyclerView it is not possible to create a new showcase
        * with that item as a target.
        * */
        GuideView.Builder(requireActivity())
            .setContentText(getString(R.string.showcase_add_purchase))
            .setDismissType(DismissType.anywhere).setTargetView(binding.fabAddPurchase)
            .setPointerType(PointerType.arrow)
            .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
            .setContentTextSize(15)
            .setGuideListener {
                val purchaseSwipeShowcase = GuideView.Builder(requireActivity())
                    .setContentText(getString(R.string.showcase_delete_purchase))
                    .setDismissType(DismissType.anywhere).setTargetView(binding.rvPurchases[0])
                    .setPointerType(PointerType.arrow)
                    .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
                    .setContentTextSize(15).setGuideListener {
                        if (viewModel.purchasesStateFlow.value.isEmpty()) {
                            // Removing the demonstrative purchase item
                            binding.rvPurchases.adapter?.asPurchasesListAdapter()
                                ?.submitList(emptyList()) {
                                    binding.rvPurchases.itemAnimator = DefaultItemAnimator()
                                    binding.tvNoData.isGone = false
                                }
                        } else {
                            // Moving back the first item to it's original place
                            binding.rvPurchases[0].clearAnimation()
                        }

                        showcaseStatus.recordShowcaseEnd(ShowcaseStatus.Screen.HOME)
                    }.build()

                GuideView.Builder(requireActivity())
                    .setContentText(getString(R.string.showcase_purchase))
                    .setDismissType(DismissType.anywhere).setTargetView(binding.rvPurchases[0])
                    .setPointerType(PointerType.arrow)
                    .setContentTypeFace(ResourcesCompat.getFont(requireContext(), R.font.iran_sans))
                    .setContentTextSize(15)
                    .setGuideListener {
                        // Moving the first item a bit to showcase how to swipe
                        val swipeAnimation =
                            AnimationUtils.loadAnimation(requireContext(), R.anim.move_to_side)
                        swipeAnimation.isFillEnabled = true
                        swipeAnimation.fillAfter = true
                        binding.rvPurchases[0].startAnimation(swipeAnimation)

                        purchaseSwipeShowcase.show()
                    }.build()
                    .show()
            }.build()
            .show()
    }

    private fun showLanguageSelectionFragmentOnFirstApplicationLaunch() {
        if (!viewModel.isApplicationLanguageSet()) {
            val action = HomeFragmentDirections.actionHomeFragmentToLanguageSelectionFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupPurchasesRecyclerView() {
        // Purchases recyclerView adapter and layout manager
        val adapter = PurchasesListAdapter(onItemClickListener = { detailedPurchase ->
            showConsumersListDialog(detailedPurchase)
        })
        binding.rvPurchases.adapter = adapter
        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())

        // Purchases recyclerView divider
        binding.rvPurchases.addItemDecoration(MaterialDividerItemDecoration(
            requireContext(), MaterialDividerItemDecoration.VERTICAL
        ).apply {
            isLastItemDecorated = false
        })

        // Purchases recyclerView item swipe for deletion
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                removePurchaseTemporarilyFromRecyclerView(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.rvPurchases)
    }

    private fun showConsumersListDialog(detailedPurchase: DetailedPurchase) {
        UiUtil.setSweetAlertDialogNightMode(resources)
        lifecycleScope.launch {
            val consumers = viewModel.getConsumerResidentsOfPurchase(detailedPurchase.purchase)
            UiUtil.createAndShowConsumersSweetDialog(requireContext(), consumers)
        }
    }

    /**
     * Removes an item from the purchases recyclerView list and shows a [Snackbar].
     * The [Snackbar] has a "Undo" button. When the "Undo" button is pressed
     * deletion will be cancelled and the recyclerVIew list will be reverted back to the real list.
     * If the [Snackbar] gets dismissed by any method other than "Undo" button, the purchase
     * gets deleted from the database.
     */
    private fun removePurchaseTemporarilyFromRecyclerView(detailedPurchaseAdapterPosition: Int) {
        /*
        * Changing purchases recyclerView list
        * to a list without the item that's going to be deleted
        * */
        val purchasesListAdapter = binding.rvPurchases.adapter!!.asPurchasesListAdapter()
        val detailedPurchases = purchasesListAdapter.currentList.toMutableList()
        val detailedPurchase = detailedPurchases[detailedPurchaseAdapterPosition]
        detailedPurchases.removeAt(detailedPurchaseAdapterPosition)
        purchasesListAdapter.submitList(detailedPurchases)

        // Showing a Snackbar with ability to undo deletion with it
        Snackbar.make(
            binding.root, getString(R.string.purchase_got_deleted), Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.undo) {
                // Reverting back the purchases recyclerView list to the original purchases list
                purchasesListAdapter.submitList(viewModel.purchasesStateFlow.value)
            }
            addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)

                    /*
                    * Deleting the purchase if the Snackbar has been dismissed by
                    * any method other than pressing the action button (the "Undo" button)
                    * */
                    if (event != DISMISS_EVENT_ACTION) {
                        viewModel.deletePurchase(detailedPurchase.purchase)
                    }
                }
            })
            show()
        }
    }

    private fun setupFab() {
        // Home fragment's floating action button shows AddPurchaseBottomSheetFragment
        binding.fabAddPurchase.setOnClickListener {
            val addPurchaseBottomSheetFragment = AddPurchaseBottomSheetFragment()
            addPurchaseBottomSheetFragment.show(
                requireActivity().supportFragmentManager, AddPurchaseBottomSheetFragment.TAG
            )
        }
    }

    private fun setupOptionsMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.home_menu, menu)
                    /*
                    * Setting the purchase list menu item for updating the icon
                    * when purchase list order gets updated
                    * */
                    purchaseListOrderMenuItem = menu.findItem(R.id.menu_order_by)
                    // Setting the item's icon for the first time
                    val purchasesOrder =
                        PurchasesOrderBy.valueOf(viewModel.purchasesOrderStateFlow.value)
                    updateOrderMenuItemIcon(purchasesOrder)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_filter -> {
                            // Opening the filter purchase fragment
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToFilterPurchasesFragment()
                            findNavController().navigate(action)
                            true
                        }
                        // Changing the purchases order
                        R.id.action_order_time_asc -> {
                            viewModel.setPurchasesOrder(PurchasesOrderBy.TIME_ASC)
                            true
                        }
                        R.id.action_order_time_desc -> {
                            viewModel.setPurchasesOrder(PurchasesOrderBy.TIME_DESC)
                            true
                        }
                        R.id.action_order_price_asc -> {
                            viewModel.setPurchasesOrder(PurchasesOrderBy.PRICE_ASC)
                            true
                        }
                        R.id.action_order_price_desc -> {
                            viewModel.setPurchasesOrder(PurchasesOrderBy.PRICE_DESC)
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.STARTED
        )
    }

    companion object UiFeedbackChannel {
        @Volatile
        private var SHOWCASE_INSTANCE: Channel<ShowcaseFeedbackType>? = null

        private fun getShowcaseFeedbackReceiver(): Flow<ShowcaseFeedbackType> {
            return getShowcaseFeedbackChannel().receiveAsFlow()
        }

        private fun getShowcaseFeedbackChannel(): Channel<ShowcaseFeedbackType> {
            return SHOWCASE_INSTANCE ?: synchronized(this) {
                if (SHOWCASE_INSTANCE == null) {
                    SHOWCASE_INSTANCE = Channel()
                }
                SHOWCASE_INSTANCE!!
            }
        }

        suspend fun startShowcase() {
            getShowcaseFeedbackChannel().send(ShowcaseFeedbackType.START_HOME_SHOWCASE)
        }

        enum class ShowcaseFeedbackType {
            START_HOME_SHOWCASE,
        }
    }
}