package com.example.travelmanager

import android.content.Context
import com.amap.api.maps.model.LatLng
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener
import com.amap.api.services.route.RouteSearch.DriveRouteQuery
import com.amap.api.services.route.RouteSearch.FromAndTo

class RouteManager(private val context: Context, private val viewModel: TravelViewModel) {

    private val routeSearch = RouteSearch(context)

    // 计算路线的时间
    fun calculateRouteTime(currentLocation: LatLng, stationLocation: LatLng, mode: String) {
        val fromAndTo = FromAndTo(
            com.amap.api.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude),
            com.amap.api.maps.model.LatLng(stationLocation.latitude, stationLocation.longitude)
        )

        val query = DriveRouteQuery(fromAndTo, RouteSearch.DriveMode.USE_TRAFFIC)
        routeSearch.setRouteSearchListener(object : OnRouteSearchListener {
            override fun onDriveRouteSearched(result: DriveRouteResult?, errorCode: Int) {
                if (errorCode == 0 && result != null) {
                    val time = result.routes[0].duration / 60  // 转化为分钟
                    viewModel.updateRouteTime(time)  // 更新路程时间
                }
            }

            // 不需要其他类型的路线搜索
            override fun onBusRouteSearched(result: BusRouteResult?, errorCode: Int) {}
            override fun onWalkRouteSearched(result: WalkRouteResult?, errorCode: Int) {}
        })

        routeSearch.calculateDriveRouteAsyn(query)
    }
}
