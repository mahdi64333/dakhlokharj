package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.data.SettingsDataStore
import ir.demoodite.dakhlokharj.enums.OrderBy
import ir.demoodite.dakhlokharj.models.database.Purchase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val purchasesStateFlow = dataRepository.purchaseDao.getAllDetailedPurchases().stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )
    val orderStateFlow = runBlocking {
        settingsDataStore.getOrderByFlow().stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            settingsDataStore.getOrderByFlow().first()
        )
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