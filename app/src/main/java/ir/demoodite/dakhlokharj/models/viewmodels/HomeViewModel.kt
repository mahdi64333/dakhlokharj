package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchasePrice
import ir.demoodite.dakhlokharj.data.DataRepository.Companion.purchaseTime
import ir.demoodite.dakhlokharj.data.SettingsDataStore
import ir.demoodite.dakhlokharj.enums.OrderBy
import ir.demoodite.dakhlokharj.models.database.DetailedPurchase
import ir.demoodite.dakhlokharj.models.database.Purchase
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
                            "TIME" -> purchaseTime
                            "PRICE" -> purchasePrice
                            else -> purchaseTime
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

        viewModelScope.launch {
            settingsDataStore.getLanguageFlow().shareIn(viewModelScope, SharingStarted.Lazily)
                .collectLatest {
                    val appLocaleList = LocaleListCompat.forLanguageTags(it)
                    AppCompatDelegate.setApplicationLocales(appLocaleList)
                }
        }
    }

    fun deletePurchase(purchase: Purchase) {
        viewModelScope.launch {
            dataRepository.purchaseDao.delete(purchase)
        }
    }

    fun setOrder(orderBy: OrderBy) {
        viewModelScope.launch {
            settingsDataStore.setOrderBy(orderBy.name)
        }
    }
}