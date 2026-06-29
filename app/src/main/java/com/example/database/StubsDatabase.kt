package com.example.database

class UserAlarm(
    val id: Int = 0,
    val timeInMillis: Long = 0,
    val isEnabled: Boolean = false,
    val hour: Int = 0,
    val minute: Int = 0,
    val amPm: String = "AM",
    val label: String = "",
    val days: List<Int> = emptyList(),
    val deleteAfterRinging: Boolean = false,
    val sound: String = "",
    val ringtoneUri: String = "",
    val snooze: String = "",
    val vibrate: Boolean = false
)

class DailyTracker(
    val date: String = "",
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val quran: Boolean = false,
    val charity: Boolean = false,
    val reading: Boolean = false,
    val istighfar: Boolean = false,
    val parents: Boolean = false,
    val tasbihCount: Int = 0
)

class TrackerDatabase {
    companion object {
        fun getDatabase(context: android.content.Context): TrackerDatabase = TrackerDatabase()
    }
    fun notificationDao(): NotificationDao = NotificationDao()
}
class NotificationDao {
    fun countByRemoteId(id: String): Int = 0
    fun insertNotification(entity: NotificationEntity) {}
    fun getAllNotifications(): kotlinx.coroutines.flow.Flow<List<NotificationEntity>> = kotlinx.coroutines.flow.emptyFlow()
}
class NotificationEntity(val title: String, val body: String, val timestamp: Long, val type: String, val actorName: String, val remoteId: String)
