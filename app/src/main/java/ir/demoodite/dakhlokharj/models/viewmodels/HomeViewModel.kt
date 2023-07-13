package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    dataRepository: DataRepository
) : ViewModel() {
    val purchasesStateFlow = dataRepository.purchaseDao.getAllDetailedPurchases().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )
}