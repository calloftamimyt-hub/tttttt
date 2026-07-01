package com.example.receiver

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.Service
import android.os.IBinder
import android.app.AlarmManager
import android.app.PendingIntent
import java.util.Calendar
import java.util.Locale
import com.example.calculator.PrayerCalculator

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            AlarmHelper.schedulePrayerReminders(context)
        }
    }
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
    ) {
        // Trigger rescheduling our reminders when location or manual settings refresh
        schedulePrayerReminders(context)
    }

    fun schedulePrayerReminders(context: Context) {
        val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        val prayerPrefs = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        val madhab = prayerPrefs.getInt("madhab", 2)
        val lat = alarmPrefs.getFloat("lat", 23.8103f).toDouble()
        val lng = alarmPrefs.getFloat("lng", 90.4125f).toDouble()
        val offset = alarmPrefs.getFloat("offset", 6.0f).toDouble()

        val todayCal = Calendar.getInstance()
        val todayTimes = PrayerCalculator.calculatePrayerTimes(lat, lng, offset, madhab, todayCal)

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowTimes = PrayerCalculator.calculatePrayerTimes(lat, lng, offset, madhab, tomorrowCal)

        val prayers = listOf(
            "Fajr" to Pair(todayTimes.fajrHours, tomorrowTimes.fajrHours),
            "Dhuhr" to Pair(todayTimes.dhuhrHours, tomorrowTimes.dhuhrHours),
            "Asr" to Pair(todayTimes.asrHours, tomorrowTimes.asrHours),
            "Maghrib" to Pair(todayTimes.maghribHours, tomorrowTimes.maghribHours),
            "Isha" to Pair(todayTimes.ishaHours, tomorrowTimes.ishaHours)
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        prayers.forEachIndexed { index, (name, hoursPair) ->
            val (todayHrs, tomorrowHrs) = hoursPair

            // Calculate today's reminder time (10 minutes before prayer time)
            val calToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayTimeMs = calToday.timeInMillis + (todayHrs * 3600 * 1000).toLong()
            val todayReminderMs = todayTimeMs - 10 * 60 * 1000

            val targetMs: Long
            if (todayReminderMs > now) {
                targetMs = todayReminderMs
            } else {
                // Use tomorrow's prayer time minus 10 minutes
                val calTomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val tomorrowTimeMs = calTomorrow.timeInMillis + (tomorrowHrs * 3600 * 1000).toLong()
                targetMs = tomorrowTimeMs - 10 * 60 * 1000
            }

            // Create Intent targeting PrayerNotificationReceiver
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = "com.example.ACTION_PRAYER_REMINDER"
                putExtra("PRAYER_NAME", name)
                putExtra("REMINDER_TIME_MS", targetMs)
            }

            val requestCode = 400 + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        targetMs,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                // Fallback to inexact set if exact permission not granted
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetMs,
                    pendingIntent
                )
            }
        }
    }
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
