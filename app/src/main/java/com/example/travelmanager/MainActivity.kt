package com.example.travelmanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.amap.api.maps.model.LatLng

class MainActivity : AppCompatActivity() {

    private val travelViewModel: TravelViewModel by viewModels()
    private lateinit var locationManager: LocationManager
    private lateinit var routeManager: RouteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = LocationManager(this, travelViewModel)
        routeManager = RouteManager(this, travelViewModel)

        // 观察位置变化
        travelViewModel.location.observe(this, Observer { location ->
            Toast.makeText(this, "当前位置: $location", Toast.LENGTH_SHORT).show()
        })

        // 启动定位
        locationManager.startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
    }
}
