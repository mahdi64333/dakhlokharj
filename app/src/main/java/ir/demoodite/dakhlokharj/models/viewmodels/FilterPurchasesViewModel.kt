package ir.demoodite.dakhlokharj.models.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.demoodite.dakhlokharj.data.DataRepository
import javax.inject.Inject

@HiltViewModel
class FilterPurchasesViewModel @Inject constructor(
    dataRepository: DataRepository,
) : ViewModel() {
}