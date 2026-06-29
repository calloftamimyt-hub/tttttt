package com.example.receiver

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.Service
import android.os.IBinder

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}
class AlarmService : Service() {
    override fun onBind(intent: Intent): IBinder? = null
}
object AlarmHelper {
    fun scheduleAlarms(context: Context) {}
    fun cancelAlarms(context: Context) {}
    fun scheduleSingleAlarm(context: Context, time: Long, id: Int) {}
    fun scheduleNextPrayer(
        context: Context,
        lat: Double = 0.0,
        lng: Double = 0.0,
        timezoneOffsetHor: Double = 0.0,
        alarms: Map<String, Boolean> = emptyMap(),
        locationName: String = "",
        isAuto: Boolean = false
    ) {}
}
class SocialBlockerService : Service() {
    override fun onBind(intent: Intent): IBinder? = null
    companion object {
        fun isPermissionGranted(context: Context): Boolean = true
        fun startService(context: Context) {}
        fun stopService(context: Context) {}
    }
}
object SilentModeHelper {
    fun setSilentMode(context: Context, enabled: Boolean) {}
    fun scheduleSilentAlarms(context: Context) {}
    fun getFajrTime(context: Context): String = ""
    fun getDhuhrTime(context: Context): String = ""
    fun getAsrTime(context: Context): String = ""
    fun getMaghribTime(context: Context): String = ""
    fun getIshaTime(context: Context): String = ""
    fun getPrayerTime(context: Context, name: String): String = ""
}
object DuroodHelper {
    fun setReminder(context: Context) {}
    fun getReminderStatus(context: Context): Pair<Boolean, Boolean> = Pair(false, false)
    fun isEnabled(context: Context): Boolean = false
    fun getIntervalMins(context: Context): Int = 60
    fun isVoiceEnabled(context: Context): Boolean = false
    fun getSelectedText(context: Context): String = ""
    fun isBusyEnabled(context: Context): Boolean = false
    fun getBusyStartMins(context: Context): Int = 0
    fun getBusyEndMins(context: Context): Int = 0
    fun getCustomVoiceUri(context: Context): String = ""
    fun saveConfig(
        context: Context,
        enabled: Boolean,
        interval: Int,
        voiceEnabled: Boolean,
        text: String,
        busyEnabled: Boolean,
        busyStartMins: Int,
        busyEndMins: Int,
        customVoiceUri: String
    ) {}
}
