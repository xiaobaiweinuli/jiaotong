package com.example.travelmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.maps.model.LatLng

class TravelViewModel : ViewModel() {
    private val _location = MutableLiveData<LatLng>()
    val location: LiveData<LatLng> get() = _location

    fun updateLocation(latLng: LatLng) {
        _location.value = latLng
    }
}
