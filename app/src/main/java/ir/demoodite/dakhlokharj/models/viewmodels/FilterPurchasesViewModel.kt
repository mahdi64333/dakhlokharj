package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.models.database.DetailedPurchase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FilterPurchasesViewModel @Inject constructor(
    dataRepository: DataRepository,
) : ViewModel() {
    private val _purchasesStateFlowList: List<MutableStateFlow<List<DetailedPurchase>>> =
        List(5) { MutableStateFlow(listOf()) }
    val purchasesStateFlowList
        get() = _purchasesStateFlowList.map {
            it.asStateFlow()
        }
    private var purchasesCollectionJobList: MutableList<Job?> = MutableList(5) { null }
}