package com.example.travelmanager

import android.content.Context
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.model.LatLng

class LocationManager(private val context: Context, private val viewModel: TravelViewModel) {

    private val locationClient = AMapLocationClient(context)

    fun startLocationUpdates() {
        val locationOption = AMapLocationClientOption()
        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationClient.setLocationOption(locationOption)

        locationClient.setLocationListener { location ->
            if (location != null && location.errorCode == 0) {
                viewModel.updateLocation(LatLng(location.latitude, location.longitude))
            } else {
                Toast.makeText(context, "定位失败：${location.errorCode}", Toast.LENGTH_SHORT).show()
            }
        }
        locationClient.startLocation()
    }

    fun stopLocationUpdates() {
        locationClient.stopLocation()
    }
}
