package com.example.traveltimeplanner

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRouteResult
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnGetRoutePlanResultListener {

    private lateinit var mapView: MapView
    private lateinit var baiduMap: BaiduMap
    private lateinit var routePlanSearch: RoutePlanSearch
    private var currentLocation: Location? = null
    private var departureAlarmSet = false
    private var boardAlarmSet = false
    private var getOffAlarmSet = false
    private var selectedTransportationMode = 0
    private var customMarginTime = 45 * 60 // 默认 45 分钟
    private var customDistanceThreshold = 1000.0 // 默认 1 千米
    private var customVibrationStrength = VibrationEffect.DEFAULT_AMPLITUDE
    private var customRingtoneUri: String? = null
    private var customMapApi = "baidu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化百度地图 SDK
        SDKInitializer.initialize(applicationContext)

        mapView = findViewById(R.id.mapView)
        baiduMap = mapView.map
        routePlanSearch = RoutePlanSearch(this)
        routePlanSearch.setOnGetRoutePlanResultListener(this)

        // 检查权限
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        // 开启定位
        baiduMap.isMyLocationEnabled = true
        val locationClient = baiduMap.location
        locationClient.start()

        // 处理信息录入方式选择
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioManualInput -> {
                    // 手动输入逻辑
                }
                R.id.radioOtherMethod -> {
                    // 其他录入方式逻辑
                }
            }
        }

        // 设置交通方式选择监听器
        spinnerTransportationMode.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTransportationMode = position
                calculateRoute()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不做任何操作
            }
        }

        // 触发路线计算
        buttonCalculate.setOnClickListener {
            calculateRoute()
        }

        // 设置出发闹钟按钮点击事件
        buttonSetDepartureAlarm.setOnClickListener {
            setDepartureAlarm()
        }

        // 设置上车闹钟按钮点击事件
        buttonSetBoardAlarm.setOnClickListener {
            setBoardAlarm()
        }

        // 设置下车闹钟按钮点击事件
        buttonSetGetOffAlarm.setOnClickListener {
            setGetOffAlarm()
        }

        // 打开设置页面按钮点击事件
        buttonOpenSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在 Activity 销毁时，关闭地图和定位服务
        mapView.onDestroy()
        baiduMap.isMyLocationEnabled = false
        val locationClient = baiduMap.location
        locationClient.stop()
    }

    override fun onPause() {
        super.onPause()
        // 在 Activity 暂停时，暂停地图
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        // 在 Activity 恢复时，恢复地图
        mapView.onResume()
    }

    private fun calculateRoute() {
        val stationLocationLatLng = LatLng(0.0, 0.0) // 根据用户输入的车站位置确定
        val startLocationLatLng = currentLocation?.let {
            LatLng(it.latitude, it.longitude)
        }?: LatLng(0.0, 0.0)

        when (selectedTransportationMode) {
            0 -> routePlanSearch.walkingSearch(
                PlanNode(startLocationLatLng),
                PlanNode(stationLocationLatLng)
            )
            1 -> routePlanSearch.bikingSearch(
                PlanNode(startLocationLatLng),
                PlanNode(stationLocationLatLng)
            )
            2 -> routePlanSearch.drivingSearch(
                PlanNode(startLocationLatLng),
                PlanNode(stationLocationLatLng)
            )
            3 -> routePlanSearch.transitSearch(
                PlanNode(startLocationLatLng),
                PlanNode(stationLocationLatLng)
            )
            else -> Toast.makeText(this, "请选择交通方式", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGetWalkingRouteResult(result: WalkingRouteResult?) {
        if (result == null || result.error!= 0) {
            Toast.makeText(this, "步行路线查询失败", Toast.LENGTH_SHORT).show()
            return
        }
        // 处理步行路线结果
        val route = result.routes[0]
        val duration = route.duration
        textViewDuration.text = "步行最长用时：$duration 秒"
    }

    override fun onGetTransitRouteResult(result: TransitRouteResult?) {
        if (result == null || result.error!= 0) {
            Toast.makeText(this, "公交路线查询失败", Toast.LENGTH_SHORT).show()
            return
        }
        // 处理公交路线结果
        val route = result.routes[0]
        val duration = route.duration
        textViewDuration.text = "公交最长用时：$duration 秒"
    }

    override fun onGetDrivingRouteResult(result: DrivingRouteResult?) {
        if (result == null || result.error!= 0) {
            Toast.makeText(this, "驾车路线查询失败", Toast.LENGTH_SHORT).show()
            return
        }
        // 处理驾车路线结果
        val route = result.routes[0]
        val duration = route.duration
        textViewDuration.text = "驾车最长用时：$duration 秒"
    }

    override fun onGetBikingRouteResult(result: BikingRouteResult?) {
        if (result == null || result.error!= 0) {
            Toast.makeText(this, "骑行路线查询失败", Toast.LENGTH_SHORT).show()
            return
        }
        // 处理骑行路线结果
        val route = result.routes[0]
        val duration = route.duration
        textViewDuration.text = "骑行最长用时：$duration 秒"
    }

    override fun onGetIndoorRouteResult(result: IndoorRouteResult?) {
        // 室内路线暂不处理
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，开启定位
                baiduMap.isMyLocationEnabled = true
                val locationClient = baiduMap.location
                locationClient.start()
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 更新当前位置
    fun updateLocation(location: Location) {
        currentLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        val myLocationData = MyLocationData.Builder()
          .latitude(location.latitude)
          .longitude(location.longitude)
          .build()
        baiduMap.setMyLocationData(myLocationData)
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng))

        if (departureAlarmSet) {
            checkDepartureAlarmStatus()
        }
        if (boardAlarmSet) {
            checkBoardAlarmStatus()
        }
        if (getOffAlarmSet) {
            checkGetOffAlarmStatus()
        }
    }

    private fun setDepartureAlarm() {
        val stationLocationLatLng = LatLng(0.0, 0.0) // 根据用户输入的车站位置确定
        val startLocationLatLng = currentLocation?.let {
            LatLng(it.latitude, it.longitude)
        }?: LatLng(0.0, 0.0)

        routePlanSearch.walkingSearch(
            PlanNode(startLocationLatLng),
            PlanNode(stationLocationLatLng)
        )

        departureAlarmSet = true
    }

    private fun setBoardAlarm() {
        val boardTime = // 根据用户输入的上车时间确定
        val alarmTime = boardTime.minusSeconds(30 * 60)

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("type", "board")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.time, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.time, pendingIntent)
        }

        boardAlarmSet = true
    }

    private fun setGetOffAlarm() {
        val getOffTime = // 根据用户输入的下车时间确定
        val alarmTime = getOffTime.minusSeconds(30 * 60)

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("type", "getOff")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.time, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.time, pendingIntent)
        }

        getOffAlarmSet = true
    }

    private fun checkDepartureAlarmStatus() {
        val stationLocationLatLng = LatLng(0.0, 0.0) // 根据用户输入的车站位置确定
        val startLocationLatLng = currentLocation?.let {
            LatLng(it.latitude, it.longitude)
        }?: LatLng(0.0, 0.0)

        routePlanSearch.walkingSearch(
            PlanNode(startLocationLatLng),
            PlanNode(stationLocationLatLng)
        )

        val routeDuration = // 获取步行路线最长用时

        val departureTime = // 根据用户输入的发车时间确定
        val alarmTime = departureTime.minusSeconds(routeDuration).minusSeconds(customMarginTime)

        if (System.currentTimeMillis() >= alarmTime.time) {
            val distance = calculateDistance(startLocationLatLng, currentLocation)
            if (distance > customDistanceThreshold) {
                textViewStatus.text = "请注意时间"
                textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
            } else {
                textViewStatus.text = "请抓紧时间"
                textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }

    private fun checkBoardAlarmStatus() {
        val boardTime = // 根据用户输入的上车时间确定
        val currentTime = Calendar.getInstance().timeInMillis

        if (currentTime >= boardTime.minusSeconds(30 * 60).time) {
            textViewStatus.text = "请注意上车"
            textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.yellow))
        }
    }

    private fun checkGetOffAlarmStatus() {
        val getOffTime = // 根据用户输入的下车时间确定
        val currentTime = Calendar.getInstance().timeInMillis

        if (currentTime >= getOffTime.minusSeconds(30 * 60).time) {
            textViewStatus.text = "请注意下车"
            textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun calculateDistance(latLng1: LatLng, location2: Location?): Double {
        val location1 = Location("").apply {
            latitude = latLng1.latitude
            longitude = latLng1.longitude
        }
        location2?.let {
            return location1.distanceTo(it)
        }
        return 0.0
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.getStringExtra("type")
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    context.customVibrationStrength,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(context.customVibrationStrength)
        }

        val ringtone = RingtoneManager.getRingtone(context, Uri.parse(context.customRingtoneUri))
        ringtone.play()

        when (type) {
            "board" -> Toast.makeText(context, "请注意上车", Toast.LENGTH_SHORT).show()
            "getOff" -> Toast.makeText(context, "请注意下车", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(context, "未知闹钟类型", Toast.LENGTH_SHORT).show()
        }
    }
}