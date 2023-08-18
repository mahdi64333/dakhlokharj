package ir.demoodite.dakhlokharj.ui.components.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRICE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_TIME
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.data.settings.enums.PurchasesOrderBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    private val _purchasesStateFlow = MutableStateFlow(listOf<DetailedPurchase>())
    val purchasesStateFlow get() = _purchasesStateFlow.asStateFlow()

    /**
     * The purchase collection job object. It must be canceled when the purchase order changes.
     */
    private var purchasesCollectionJob: Job? = null
    val purchasesOrderStateFlow = runBlocking {
        settingsDataStore.getOrderByFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            settingsDataStore.getOrderByFlow().first()
        )
    }

    init {
        viewModelScope.launch {
            settingsDataStore.getOrderByFlow().collectLatest {
                // Canceling previous purchases collection with the old purchases order
                purchasesCollectionJob?.cancel()

                // Constructing required order of purchases
                val purchasesOrderBy = PurchasesOrderBy.valueOf(it)
                // The column which order must be based upon
                val orderColumn = when (purchasesOrderBy.name.split("_").first()) {
                    "TIME" -> PURCHASE_TIME
                    "PRICE" -> PURCHASE_PRICE
                    else -> PURCHASE_TIME
                }
                // Ascending or descending order of purchases
                val order = purchasesOrderBy.name.split("_").last()

                // Creating a new purchase collection job based on the new order
                purchasesCollectionJob = launch {
                    dataRepository.purchaseDao.getAllDetailedPurchases(orderColumn, order)
                        .collectLatest { detailedPurchases ->
                            ensureActive()
                            _purchasesStateFlow.update {
                                detailedPurchases
                            }
                        }
                }
            }
        }
    }

    fun deletePurchase(purchase: Purchase) {
        viewModelScope.launch {
            dataRepository.purchaseDao.delete(purchase)
        }
    }

    suspend fun getConsumerResidentsOfPurchase(purchase: Purchase): List<Resident> {
        return dataRepository.consumerDao.getConsumerResidentsOfPurchase(purchase.id).first()
    }

    fun setPurchasesOrder(purchasesOrderBy: PurchasesOrderBy) {
        viewModelScope.launch {
            settingsDataStore.setOrderBy(purchasesOrderBy.name)
        }
    }

    fun isApplicationLanguageSet(): Boolean {
        /*
        * Using runBlocking doesn't effect the ui thread in a noticeable way
        * because it's just a small string.
         */
        return runBlocking {
            settingsDataStore.getLanguageFlow().first().isNotEmpty()
        }
    }
}