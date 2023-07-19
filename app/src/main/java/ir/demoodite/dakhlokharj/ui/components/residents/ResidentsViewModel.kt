package ir.demoodite.dakhlokharj.ui.components.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentsViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    val residentsStateFlow = dataRepository.residentDao.getAll().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )
    private val _residentNameInputErrorResChannel = Channel<Int>()
    val residentNameInputErrorResChannel get() = _residentNameInputErrorResChannel.receiveAsFlow()
    private val _residentNameEditErrorResChannel = Channel<Int?>()
    val residentNameEditErrorResChannel get() = _residentNameEditErrorResChannel.receiveAsFlow()

    fun insertResident(resident: Resident) {
        viewModelScope.launch {
            val id = dataRepository.residentDao.insert(resident)
            if (id <= 0) {
                delay(2000)
                _residentNameInputErrorResChannel.send(R.string.duplicate)
            }
        }
    }

    fun updateResident(resident: Resident) {
        viewModelScope.launch {
            val rowsAffected = dataRepository.residentDao.update(resident)
            if (rowsAffected <= 0) {
                _residentNameEditErrorResChannel.send(R.string.duplicate)
            } else {
                _residentNameEditErrorResChannel.send(null)
            }
        }
    }

    fun deleteResident(resident: Resident) {
        viewModelScope.launch {
            dataRepository.residentDao.delete(resident)
        }
    }
}