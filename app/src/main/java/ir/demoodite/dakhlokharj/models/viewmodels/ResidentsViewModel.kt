package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import ir.demoodite.dakhlokharj.models.AsyncOperationStatus
import ir.demoodite.dakhlokharj.models.database.Resident
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentsViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    val residentsStateFlow = dataRepository.residentDao.getAll().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )
    lateinit var editingResident: Resident

    fun insertEditingResident(status: MutableSharedFlow<AsyncOperationStatus>) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dataRepository.residentDao.insert(editingResident)
            status.emit(
                AsyncOperationStatus(
                    id >= 0
                )
            )
        }
    }

    fun updateEditingResident(status: MutableSharedFlow<AsyncOperationStatus>) {
        viewModelScope.launch(Dispatchers.IO) {
            val rowsAffected = dataRepository.residentDao.update(editingResident)
            status.emit(
                AsyncOperationStatus(
                    rowsAffected > 0
                )
            )
        }
    }

    fun deleteResident(resident: Resident) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.residentDao.delete(resident)
        }
    }
}