package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.models.database.Purchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    val purchasesStateFlow = dataRepository.purchaseDao.getAllDetailedPurchases().stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )

    fun deletePurchase(purchase: Purchase) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.purchaseDao.delete(purchase)
        }
    }
}