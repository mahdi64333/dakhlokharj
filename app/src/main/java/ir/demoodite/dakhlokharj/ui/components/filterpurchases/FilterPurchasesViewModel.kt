package ir.demoodite.dakhlokharj.ui.components.filterpurchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.DetailedPurchase
import ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters.FilterBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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

    fun getFilteredPurchasesStateFlow(filterBy: FilterBy) =
        _purchasesStateFlows[filterBy.ordinal]

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
}