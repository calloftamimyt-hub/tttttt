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
}
