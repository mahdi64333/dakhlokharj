package ir.demoodite.dakhlokharj.ui.components.filterpurchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters.FilterBy
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
    private val _purchasesStateFlows: List<MutableStateFlow<Pair<List<DetailedPurchase>, Long>?>> =
        List(FilterBy.size) { MutableStateFlow(null) }
    private var purchasesCollectionJobs: MutableList<Job?> =
        MutableList(_purchasesStateFlows.size) { null }
    val residentsStateFlow: StateFlow<List<Resident>> by lazy {
        dataRepository.residentDao.getAll().stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            listOf()
        )
    }
    val residents get() = residentsStateFlow.value
    private val _deletionChannel = Channel<PurchaseDeleteType>()
    val deletionChannel get() = _deletionChannel.receiveAsFlow()
    private var pendingPurchaseAndConsumersToDelete: Pair<Purchase, List<Consumer>>? = null
    private var pendingPurchasesAndConsumersToDelete: List<Pair<Purchase, List<Consumer>>>? = null
    private val _filteredPurchasesChangedChannel = Channel<FilterBy>()
    val filteredPurchasesChangedChannel get() = _filteredPurchasesChangedChannel.receiveAsFlow()

    fun getFilteredPurchasesStateFlow(filterType: FilterBy) =
        _purchasesStateFlows[filterType.ordinal]

    fun filterByProductName(productName: String) {
        val filterIndex = FilterBy.PRODUCT_NAME.ordinal

        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByProductName(productName)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        val sum = it.fold(0L) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }

                }
        }
    }

    fun filterByPrice(minPrice: Long, maxPrice: Long) {
        val filterIndex = FilterBy.PRICE.ordinal

        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByPrice(minPrice, maxPrice)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        val sum = it.fold(0L) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                }
        }
    }

    fun filterByBuyer(buyerId: Long) {
        val filterIndex = FilterBy.BUYER.ordinal

        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByBuyer(buyerId)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        val sum = it.fold(0L) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                }
        }
    }

    fun filterByTime(startTime: Long, endTime: Long) {
        val filterIndex = FilterBy.TIME.ordinal

        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByTime(startTime, endTime)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        val sum = it.fold(0L) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                }
        }
    }

    fun filterByConsumer(consumerId: Long) {
        val filterIndex = FilterBy.CONSUMER.ordinal

        purchasesCollectionJobs[filterIndex]?.cancel()
        viewModelScope.launch {
            dataRepository.purchaseDao.getAllDetailedPurchasesByConsumer(consumerId)
                .collectLatest {
                    ensureActive()
                    _purchasesStateFlows[filterIndex].update { _ ->
                        val sum = it.fold(0L) { acc, next ->
                            acc + next.purchasePrice
                        }
                        Pair(it, sum)
                    }
                }
        }
    }

    fun requestPurchaseDelete(purchase: Purchase) {
        viewModelScope.launch {
            val consumers = dataRepository.consumerDao.getConsumersOfPurchase(purchase.id).first()
            val purchaseAndConsumers = Pair(purchase, consumers)
            dataRepository.purchaseDao.delete(purchase)
            pendingPurchaseAndConsumersToDelete = purchaseAndConsumers
            _deletionChannel.send(PurchaseDeleteType.SINGLE_PURCHASE)
        }
    }

    fun undoPurchaseDelete() {
        viewModelScope.launch {
            pendingPurchaseAndConsumersToDelete?.let {
                dataRepository.purchaseDao.insert(it.first)
                dataRepository.consumerDao.insert(it.second)
            }
        }
    }

    fun notifyFilteredPurchasesChanged(filterType: FilterBy) {
        viewModelScope.launch {
            _filteredPurchasesChangedChannel.send(filterType)
        }
    }

    fun requestPurchasesDelete(purchases: List<Purchase>) {
        viewModelScope.launch {
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

    fun undoPurchasesDelete() {
        viewModelScope.launch {
            pendingPurchasesAndConsumersToDelete?.forEach {
                dataRepository.purchaseDao.insert(it.first)
                dataRepository.consumerDao.insert(it.second)
            }
        }
    }

    enum class PurchaseDeleteType {
        SINGLE_PURCHASE,
        MULTIPLE_PURCHASE,
    }
}