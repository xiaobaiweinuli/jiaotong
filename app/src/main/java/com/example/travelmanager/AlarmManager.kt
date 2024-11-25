package com.example.travelmanager

import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.RingtoneManager
import android.app.AlarmManager
import android.app.PendingIntent

class AlarmManager(private val context: Context) {

    fun setAlarm(timeInMillis: Long, onAlarmTriggered: () -> Unit) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        // 注册广播接收器，当闹钟触发时调用
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                onAlarmTriggered()
            }
        }
        context.registerReceiver(receiver, IntentFilter(AlarmReceiver.ALARM_ACTION))
    }

    // 自定义音频或震动提醒
    fun playAlarmSoundOrVibrate(vibrationIntensity: Int) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, vibrationIntensity))
        }

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(context, notification)
        ringtone.play()
    }
}
