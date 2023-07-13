package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.models.database.Purchase
import ir.demoodite.dakhlokharj.models.database.Resident
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    val activeResidentsStateFlow = dataRepository.residentDao.getAllActive().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )
    private var _selectedResidentsStateFlow: MutableStateFlow<Set<Resident>> =
        MutableStateFlow(setOf())
    val selectedResidentsStateFlow get() = _selectedResidentsStateFlow.asStateFlow()
    val savedPurchaseInfo = Purchase(
        -1,
        "",
        -1,
        -1,
        -1,
    )

    fun addSelectedResident(resident: Resident) {
        _selectedResidentsStateFlow.update {
            it.toMutableSet().apply {
                add(resident)
            }.toSet()
        }
    }

    fun removeSelectedResident(resident: Resident) {
        _selectedResidentsStateFlow.update {
            it.toMutableSet().apply {
                remove(resident)
            }.toSet()
        }
    }
}