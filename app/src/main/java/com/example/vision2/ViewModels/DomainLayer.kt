package com.example.vision2.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vision2.dataClass.userLocation
import com.example.vision2.repository.appRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DomainLayer @Inject constructor(val repository: appRepository): ViewModel() {

    val userLocationItem: StateFlow<List<userLocation>>
        get() = repository.resLocation

    private val _receivedData = mutableStateOf(0)
    val receivedData: State<Int> = _receivedData

    fun updateReceivedData(value: Int) {
        _receivedData.value = value
    }

    init {
        viewModelScope.launch {
            repository.getUserLocation()
        }
    }

}