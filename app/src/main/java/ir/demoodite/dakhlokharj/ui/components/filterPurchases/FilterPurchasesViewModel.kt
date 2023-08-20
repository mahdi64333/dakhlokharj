package ir.demoodite.dakhlokharj.ui.components.filterPurchases

import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters.PurchaseFilters
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterPurchasesViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    // List of all filters StateFlows containing purchases and their price sum
    private val _purchasesStateFlows: List<MutableStateFlow<Pair<List<DetailedPurchase>, Double>?>> =
        List(PurchaseFilters.size) { MutableStateFlow(null) }

    /**
     * All filtered purchases collection jobs List.
     * Each of them must be canceled when the associated filter changes.
     */
    private var purchasesCollectionJobs: MutableList<Job?> =
        MutableList(PurchaseFilters.size) { null }

    // Residents StateFlow
    val residentsStateFlow: StateFlow<List<Resident>> by lazy {
        dataRepository.residentDao.getAllNonDeleted().stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
    }

    /**
     * List of all residents.
     */
    val residents get() = residentsStateFlow.value

    // Deletion Channel
    private val _deletionChannel = Channel<PurchaseDeleteType>()

    /**
     * Channel for sending delete events to ui.
     */
    val deletionChannel get() = _deletionChannel.receiveAsFlow()

    /**
     * A Purchase and it's consumers that are going to get deleted.
     */
    private var pendingPurchaseAndConsumersToDelete: Pair<Purchase, List<Consumer>>? = null

    /**
     * Purchases and their consumers that are going to get deleted.
     */
    private var pendingPurchasesAndConsumersToDelete: List<Pair<Purchase, List<Consumer>>>? = null

    private val _filteredPurchasesChangedChannel = Channel<PurchaseFilters>()
    val filteredPurchasesChangedChannel get() = _filteredPurchasesChangedChannel.receiveAsFlow()

    /**
     * Get the [StateFlow] of purchases associated with the given [PurchaseFilters].
     *
     * @param filterType The [PurchaseFilters] of purchases.
     * @return [StateFlow] for purchases of requested filter.
     */
    fun getFilteredPurchasesStateFlow(filterType: PurchaseFilters) =
        _purchasesStateFlows[filterType.ordinal]

    /**
     * Set the name of purchases that are being filtered by product name.
     */
    fun filterByProductName(productName: String) {
        val filter = PurchaseFilters.PRODUCT_NAME
        val filterIndex = PurchaseFilters.PRODUCT_NAME.ordinal

        // Canceling the previous job
        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByProductName(productName)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        // Calculating all purchases price sum
                        val sum: Double = it.fold(0.0) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                    notifyFilteredPurchasesChanged(filter)
                }
        }
    }

    /**
     * Set the price range of purchases that are being filtered by price.
     */
    fun filterByPrice(minPrice: Double, maxPrice: Double) {
        val filter = PurchaseFilters.PRICE
        val filterIndex = PurchaseFilters.PRICE.ordinal

        // Canceling the previous job
        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByPrice(minPrice, maxPrice)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        // Calculating all purchases price sum
                        val sum: Double = it.fold(0.0) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                    notifyFilteredPurchasesChanged(filter)
                }
        }
    }

    /**
     * Set the resident id of purchases that are being filtered by buyer.
     */
    fun filterByBuyer(buyerId: Long) {
        val filter = PurchaseFilters.BUYER
        val filterIndex = PurchaseFilters.BUYER.ordinal

        // Canceling the previous job
        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByBuyer(buyerId)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        // Calculating all purchases price sum
                        val sum: Double = it.fold(0.0) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                    notifyFilteredPurchasesChanged(filter)
                }
        }
    }

    /**
     * Set the time range of purchases that are being filtered by time.
     */
    fun filterByTime(startTime: Long, endTime: Long) {
        val filter = PurchaseFilters.TIME
        val filterIndex = PurchaseFilters.TIME.ordinal

        // Canceling the previous job
        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByTime(startTime, endTime)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        // Calculating all purchases price sum
                        val sum: Double = it.fold(0.0) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                    notifyFilteredPurchasesChanged(filter)
                }
        }
    }

    /**
     * Set the resident id of purchases that are being filtered by consumer.
     */
    fun filterByConsumer(consumerId: Long) {
        val filter = PurchaseFilters.CONSUMER
        val filterIndex = PurchaseFilters.CONSUMER.ordinal

        // Canceling the previous job
        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByConsumer(consumerId)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        // Calculating all purchases price sum
                        val sum: Double = it.fold(0.0) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                    notifyFilteredPurchasesChanged(filter)
                }
        }
    }

    /**
     * Request to delete a single purchase item. This deletion is temporary and by calling
     * [undoPurchaseDelete] the purchase delete can be reverted.
     *
     * @param purchase The purchase to delete.
     */
    fun requestPurchaseDelete(purchase: Purchase) {
        viewModelScope.launch {
            // Finding consumers of the purchase
            val consumers = dataRepository.consumerDao.getConsumersOfPurchase(purchase.id).first()
            // Saving data of all deleting records
            pendingPurchaseAndConsumersToDelete = Pair(purchase, consumers)

            dataRepository.purchaseDao.delete(purchase)

            // Sending an event to ui to show a Snackbar with undo button
            _deletionChannel.send(PurchaseDeleteType.SINGLE_PURCHASE)
        }
    }

    /**
     * Reverts back the last purchase deletion by [requestPurchaseDelete].
     */
    fun undoPurchaseDelete() {
        viewModelScope.launch {
            pendingPurchaseAndConsumersToDelete?.let { (purchase, consumers) ->
                val newPurchaseId = dataRepository.purchaseDao.insert(purchase)
                consumers.forEach { consumer ->
                    consumer.productId = newPurchaseId
                }
                dataRepository.consumerDao.insert(consumers)
            }
        }
    }

    /**
     * Request to delete all entries from current list of filtered purchases by [filterType].
     * This deletion is temporary and by calling [undoBatchPurchaseDelete]
     * the purchase delete can be reverted.
     *
     * @param filterType The filter type to delete all purchases associated with it.
     * In other words, this method deletes all purchases that are being shown the related
     * filter tab.
     */
    fun requestBatchFilteredPurchasesDelete(filterType: PurchaseFilters) {
        viewModelScope.launch {
            val purchases =
                getFilteredPurchasesStateFlow(filterType).value?.first?.map {
                    it.purchase
                } ?: listOf()
            val purchasesAndConsumers = purchases.map { purchase ->
                val consumers =
                    dataRepository.consumerDao.getConsumersOfPurchase(purchase.id).first()
                Pair(purchase, consumers)
            }
            dataRepository.purchaseDao.delete(purchases)
            pendingPurchasesAndConsumersToDelete = purchasesAndConsumers
            _deletionChannel.send(PurchaseDeleteType.MULTIPLE_PURCHASE)
        }
    }

    /**
     * Reverts back the last batch purchase deletion by [requestBatchFilteredPurchasesDelete].
     */
    fun undoBatchPurchaseDelete() {
        viewModelScope.launch {
            pendingPurchasesAndConsumersToDelete?.forEach {
                dataRepository.purchaseDao.insert(it.first)
                dataRepository.consumerDao.insert(it.second)
            }
        }
    }

    /**
     * Emits a signal to [filteredPurchasesChangedChannel]. Useful for updating the "Delete all"
     * [MenuItem] of the [FilterPurchasesFragment].
     *
     * @param filterType The filter that it
     */
    private fun notifyFilteredPurchasesChanged(filterType: PurchaseFilters) {
        viewModelScope.launch {
            _filteredPurchasesChangedChannel.send(filterType)
        }
    }

    /**
     * Because this ViewModel is used as a shared ViewModel, the lists must be cleared. this
     * methode clears all stateFlows and cancels all data collection jobs.
     */
    fun clearStateFlows() {
        // Clearing lists
        _purchasesStateFlows.forEach {
            it.value = null
        }

        // Canceling collection jobs
        purchasesCollectionJobs.forEach {
            it?.cancel()
        }
    }

    enum class PurchaseDeleteType {
        SINGLE_PURCHASE,
        MULTIPLE_PURCHASE,
    }
}