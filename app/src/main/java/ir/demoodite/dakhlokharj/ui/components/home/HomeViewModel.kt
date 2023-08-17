package ir.demoodite.dakhlokharj.ui.components.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_PRICE
import ir.demoodite.dakhlokharj.data.room.DataRepository.Companion.PURCHASE_TIME
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.workers.DeletePurchaseWorker
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.data.settings.enums.OrderBy
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
    private val workManager: WorkManager,
) : ViewModel() {
    private val _purchasesStateFlow = MutableStateFlow(listOf<DetailedPurchase>())
    val purchasesStateFlow get() = _purchasesStateFlow.asStateFlow()
    private var purchasesCollectionJob: Job? = null
    val orderStateFlow = runBlocking {
        settingsDataStore.getOrderByFlow().stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            settingsDataStore.getOrderByFlow().first()
        )
    }

    init {
        viewModelScope.launch {
            settingsDataStore.getOrderByFlow().collectLatest {
                try {
                    val orderBy = OrderBy.valueOf(it)
                    purchasesCollectionJob?.cancel()
                    purchasesCollectionJob = launch {
                        val orderColumn = when (orderBy.name.split("_").first()) {
                            "TIME" -> PURCHASE_TIME
                            "PRICE" -> PURCHASE_PRICE
                            else -> PURCHASE_TIME
                        }
                        val order = orderBy.name.split("_").last()
                        dataRepository.purchaseDao.getAllDetailedPurchases(orderColumn, order)
                            .collectLatest { detailedPurchases ->
                                ensureActive()
                                _purchasesStateFlow.update {
                                    detailedPurchases
                                }
                            }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deletePurchase(purchase: Purchase) {
        val data = Data.Builder()
            .putLong(DeletePurchaseWorker.PURCHASE_ID_KEY, purchase.id)
            .build()
        val deletePurchaseWorker = OneTimeWorkRequest.Builder(DeletePurchaseWorker::class.java)
            .setInputData(data)
            .build()
        workManager.enqueue(deletePurchaseWorker)
    }

    fun setOrder(orderBy: OrderBy) {
        viewModelScope.launch {
            settingsDataStore.setOrderBy(orderBy.name)
        }
    }

    fun noLanguageSelected(): Boolean {
        return runBlocking {
            settingsDataStore.getLanguageFlow().first().isEmpty()
        }
    }
}