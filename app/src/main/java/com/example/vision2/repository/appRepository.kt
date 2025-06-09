package com.example.vision2.repository

import com.example.vision2.dataClass.userLocation
import com.example.vision2.modules.LocationCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class appRepository @Inject constructor(var apiService: LocationCall) {
    private val _location = MutableStateFlow<List<userLocation>>(emptyList())
    val resLocation: StateFlow<List<userLocation>>
        get() = _location


    suspend fun getUserLocation(){
        try {
            val response = apiService.getLocation()
            if (response.isSuccessful){
                _location.emit(response.body()!!)
            }

        }catch ( e: Exception){
            _location.emit(emptyList())
        }


    }
}