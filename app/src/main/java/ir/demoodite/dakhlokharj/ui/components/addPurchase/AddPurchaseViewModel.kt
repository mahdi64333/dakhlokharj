package ir.demoodite.dakhlokharj.ui.components.addPurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    val residentsStateFlow = dataRepository.residentDao.getAllNonDeleted().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )
    val activeResidentsStateFlow = dataRepository.residentDao.getAllActive().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )
    private var _selectedResidentsStateFlow: MutableStateFlow<Set<Resident>> =
        MutableStateFlow(emptySet())
    val selectedResidentsStateFlow get() = _selectedResidentsStateFlow.asStateFlow()

    /**
     * Data of last input fields after dismissing [AddPurchaseBottomSheetFragment]
     * to restore when reopened.
     */
    val savedPurchaseInfo = Purchase()

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

    fun clearSelectedResidents() {
        _selectedResidentsStateFlow.update {
            setOf()
        }
    }

    fun savePurchase(purchase: Purchase, consumerResidents: List<Resident>) {
        viewModelScope.launch {
            val purchaseId = dataRepository.purchaseDao.insert(purchase)
            val consumers = consumerResidents.map {
                Consumer(purchaseId, it.id)
            }
            dataRepository.consumerDao.insert(consumers)
        }
    }
}