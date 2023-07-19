package ir.demoodite.dakhlokharj.ui.components.summery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.ResidentSummery
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummeryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    val residentsSummariesStateFlow = dataRepository.residentDao.getAllSummaries().stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )
    private val _isFilteringStateFlow = MutableStateFlow(false)
    val isFilteringStateFlow get() = _isFilteringStateFlow.asStateFlow()
    private val _filteredResidentsSummariesStateFlow =
        MutableStateFlow(listOf<ResidentSummery>())
    val filteredResidentsSummariesStateFlow
        get() = _filteredResidentsSummariesStateFlow.asStateFlow()
    private var filteredResidentsSummariesCollectionJob: Job? = null

    fun toggleFiltering() {
        _isFilteringStateFlow.update {
            !it
        }
    }

    fun setSummariesTimeWindow(startTime: Long, endTime: Long) {
        filteredResidentsSummariesCollectionJob?.cancel()
        filteredResidentsSummariesCollectionJob = viewModelScope.launch {
            dataRepository.residentDao.getAllSummariesBetween(startTime, endTime)
                .collectLatest { filteredSummaries ->
                    ensureActive()
                    _filteredResidentsSummariesStateFlow.update {
                        filteredSummaries
                    }
                }
        }
    }
}