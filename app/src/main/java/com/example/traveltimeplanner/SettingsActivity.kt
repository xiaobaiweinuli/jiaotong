package com.example.traveltimeplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 设置自定义参数的逻辑
        buttonSaveSettings.setOnClickListener {
            // 获取用户输入的自定义参数
            val newMarginTime = editTextMarginTime.text.toString().toInt()
            val newDistanceThreshold = editTextDistanceThreshold.text.toString().toDouble()
            val newVibrationStrength = editTextVibrationStrength.text.toString().toInt()
            val newRingtoneUri = editTextRingtoneUri.text.toString()
            val newMapApi = spinnerMapApi.selectedItem.toString()

            // 更新全局变量
            MainActivity.customMarginTime = newMarginTime
            MainActivity.customDistanceThreshold = newDistanceThreshold
            MainActivity.customVibrationStrength = newVibrationStrength
            MainActivity.customRingtoneUri = newRingtoneUri
            MainActivity.customMapApi = newMapApi

            finish()
        }
    }
}