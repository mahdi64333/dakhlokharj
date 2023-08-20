package ir.demoodite.dakhlokharj.ui.components.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentsViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {
    val residentsStateFlow = dataRepository.residentDao.getAllNonDeleted().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), listOf()
    )
    private val _residentOperationChannel = Channel<ResidentOperationResult>()

    /**
     * Channel for sending resident database operation results.
     */
    val residentOperationChannel get() = _residentOperationChannel.receiveAsFlow()

    fun insertResident(resident: Resident) {
        viewModelScope.launch {
            if (!dataRepository.residentDao.isNameTaken(resident.name)) {
                dataRepository.residentDao.insert(resident)
                _residentOperationChannel.send(
                    ResidentOperationResult(
                        true,
                        ResidentOperationType.INSERT,
                    )
                )
            } else {
                _residentOperationChannel.send(
                    ResidentOperationResult(
                        false,
                        ResidentOperationType.INSERT,
                        R.string.there_is_a_resident_with_this_name
                    )
                )
            }
        }
    }

    fun updateResident(resident: Resident) {
        viewModelScope.launch {
            if (!dataRepository.residentDao.isNameTaken(resident.name)) {
                dataRepository.residentDao.update(resident)
                _residentOperationChannel.send(
                    ResidentOperationResult(
                        true,
                        ResidentOperationType.UPDATE,
                    )
                )
            } else {
                _residentOperationChannel.send(
                    ResidentOperationResult(
                        false,
                        ResidentOperationType.UPDATE,
                        R.string.there_is_a_resident_with_this_name
                    )
                )
            }
        }
    }

    fun deleteResident(resident: Resident) {
        viewModelScope.launch {
            dataRepository.residentDao.delete(resident)
        }
    }

    data class ResidentOperationResult(
        val isSuccessful: Boolean,
        val operationType: ResidentOperationType,
        val messageStringRes: Int? = null,
    )

    enum class ResidentOperationType {
        INSERT, UPDATE,
    }
}