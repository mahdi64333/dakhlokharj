package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SummeryViewModel @Inject constructor(
    dataRepository: DataRepository,
) : ViewModel() {
    private val _isFilteringStateFlow = MutableStateFlow<Boolean>(false)
    val isFilteringStateFlow get() = _isFilteringStateFlow.asStateFlow()

    fun toggleFiltering() {
        _isFilteringStateFlow.update {
            !it
        }
    }
}