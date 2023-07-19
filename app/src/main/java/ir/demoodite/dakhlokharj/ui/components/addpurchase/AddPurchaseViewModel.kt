package ir.demoodite.dakhlokharj.ui.components.addpurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    val residentsStateFlow: StateFlow<List<Resident>> by lazy {
        runBlocking {
            dataRepository.residentDao.getAll().stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                dataRepository.residentDao.getAll().first()
            )
        }
    }
    val activeResidentsStateFlow: StateFlow<List<Resident>> by lazy {
        runBlocking {
            dataRepository.residentDao.getAllActive().stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                dataRepository.residentDao.getAllActive().first()
            )
        }
    }
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

    fun clearSelectedResident() {
        _selectedResidentsStateFlow.update {
            setOf()
        }
    }

    fun savePurchaseRecord(purchase: Purchase, consumerResidents: List<Resident>) {
        viewModelScope.launch {
            val purchaseId = dataRepository.purchaseDao.insert(purchase)
            val consumers = consumerResidents.map {
                Consumer(purchaseId, it.id)
            }
            dataRepository.consumerDao.insert(consumers)
        }
    }
}