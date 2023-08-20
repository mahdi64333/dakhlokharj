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
    // All residents summaries StateFlows
    val residentsSummariesStateFlow = dataRepository.residentDao.getAllSummaries().stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )

    // Filtered residents summaries StateFlows
    private val _filteredResidentsSummariesStateFlow =
        MutableStateFlow<List<ResidentSummery>?>(null)
    val filteredResidentsSummariesStateFlow
        get() = _filteredResidentsSummariesStateFlow.asStateFlow()

    /**
     * The filtered summaries collection job object.
     * It must be canceled when the filter changes.
     */
    private var filteredResidentsSummariesCollectionJob: Job? = null

    private val _isFilteringStateFlow = MutableStateFlow(false)

    /**
     * Filtering ui state. True means the filtered ui is active
     * and false means normal ui for all summaries is active.
     */
    val isFilteringStateFlow get() = _isFilteringStateFlow.asStateFlow()

    /**
     * Toggles the state of filter ui. Cycles between showing all resident summaries
     * and filtered ones.
     */
    fun toggleFiltering() {
        _isFilteringStateFlow.update {
            !it
        }
    }

    /**
     * Sets the start and end time of filtered summaries.
     *
     * @param startTime Start of the filter range
     * @param endTime End of the filter range
     */
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