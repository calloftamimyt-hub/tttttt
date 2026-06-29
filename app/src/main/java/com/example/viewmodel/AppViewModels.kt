package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.database.DailyTracker

object AppLanguage {
    const val BENGALI = "bn"
    const val ENGLISH = "en"
}
class SettingsViewModel : ViewModel() {
    val selectedCountryCode: StateFlow<String> = MutableStateFlow("BD")
    val language: StateFlow<String> = MutableStateFlow("en")
}
class AlarmViewModel : ViewModel() {
    val alarms: StateFlow<List<com.example.database.UserAlarm>> = MutableStateFlow(emptyList())
    fun toggleAlarm(alarm: com.example.database.UserAlarm) {}
    fun deleteAlarm(alarm: com.example.database.UserAlarm) {}
    fun addAlarm(alarm: com.example.database.UserAlarm) {}
}
class TrackerViewModel : ViewModel() {
    val uiState: StateFlow<Any> = MutableStateFlow(Any())
    val history: StateFlow<List<DailyTracker>> = MutableStateFlow(emptyList())
    fun saveTracker(tracker: DailyTracker) {}
}
class AppLanguage {
    val code: String = "en"
    val name: String = "English"
}
