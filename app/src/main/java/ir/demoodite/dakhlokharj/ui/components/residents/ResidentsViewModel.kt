package ir.demoodite.dakhlokharj.ui.components.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Resident
import ir.demoodite.dakhlokharj.data.room.workers.DeleteResidentWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentsViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val workManager: WorkManager,
) : ViewModel() {
    val residentsStateFlow = dataRepository.residentDao.getAllNonDeleted().stateIn(
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
            if (!dataRepository.residentDao.isNameTaken(resident.name)) {
                dataRepository.residentDao.insert(resident)
            } else {
                _residentNameInputErrorResChannel.send(R.string.duplicate)
            }
        }
    }

    fun updateResident(resident: Resident) {
        viewModelScope.launch {
            if (!dataRepository.residentDao.isNameTaken(resident.name)) {
                dataRepository.residentDao.update(resident)
            } else {
                _residentNameEditErrorResChannel.send(R.string.duplicate)
            }
        }
    }

    fun deleteResident(resident: Resident) {
        val data = Data.Builder()
            .putLong(DeleteResidentWorker.RESIDENT_ID_KEY, resident.id)
            .build()
        val deleteResidentWorker = OneTimeWorkRequest.Builder(DeleteResidentWorker::class.java)
            .setInputData(data)
            .build()
        workManager.enqueue(deleteResidentWorker)
    }
}