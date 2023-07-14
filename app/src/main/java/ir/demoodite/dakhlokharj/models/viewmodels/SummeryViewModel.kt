package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SummeryViewModel @Inject constructor(
    dataRepository: DataRepository,
) : ViewModel() {
    val residentsSummariesStateFlow = dataRepository.residentDao.getAllSummaries().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )
    private val _isFilteringStateFlow = MutableStateFlow(false)
    val isFilteringStateFlow get() = _isFilteringStateFlow.asStateFlow()

    fun toggleFiltering() {
        _isFilteringStateFlow.update {
            !it
        }
    }
}