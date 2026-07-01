package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.viewmodel.AppLanguages

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ACTION_PRAYER_REMINDER") {
            val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
            showNotification(context, prayerName)
            
            // Trigger rescheduling to keep reminders updated
            AlarmHelper.schedulePrayerReminders(context)
        }
    }

    private fun showNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_reminder_channel"

        // Create notification channel if Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers reminders 10 minutes before prayer times"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Detect language setting
        val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val languageCode = settingsPrefs.getString("app_language", AppLanguages.BENGALI) ?: AppLanguages.BENGALI
        val isEng = languageCode == AppLanguages.ENGLISH

        // Translate prayer names and prepare message body
        val prayerDisplayName = when (prayerName) {
            "Fajr" -> if (isEng) "Fajr" else "ফজর"
            "Dhuhr" -> if (isEng) "Dhuhr" else "যোহর"
            "Asr" -> if (isEng) "Asr" else "আসর"
            "Maghrib" -> if (isEng) "Maghrib" else "মাগরিব"
            "Isha" -> if (isEng) "Isha" else "এশা"
            else -> prayerName
        }

        val title = if (isEng) "Upcoming Prayer Reminder" else "নামাজের রিমাইন্ডার"
        val body = if (isEng) {
            "$prayerDisplayName prayer is starting in 10 minutes. Prepare yourself for the prayer."
        } else {
            "আর মাত্র ১০ মিনিট পর $prayerDisplayName-এর ওয়াক্ত শুরু হবে। নামাজের প্রস্তুতি নিন।"
        }

        // Tap action: open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification builder with classic icon and green theme
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF10B982.toInt()) // Halal Circle Primary Green

        // Display notification (Using a unique notification ID per prayer to avoid overwriting)
        val notificationId = when (prayerName) {
            "Fajr" -> 1001
            "Dhuhr" -> 1002
            "Asr" -> 1003
            "Maghrib" -> 1004
            "Isha" -> 1005
            else -> 1000
        }
        notificationManager.notify(notificationId, builder.build())
    }
}
