package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.database.DailyTracker

class SettingsViewModel(context: android.content.Context) : ViewModel() {
    val selectedCountryCode: StateFlow<String> = MutableStateFlow("BD")
    val language: StateFlow<String> = MutableStateFlow(AppLanguages.BENGALI)
    fun setSelectedCountryAndLanguage(countryCode: String, languageCode: String = AppLanguages.BENGALI) {}
    fun toggleAlarm(context: android.content.Context, alarmId: String) {}
}
class AlarmViewModel(context: android.content.Context) : ViewModel() {
    val alarms: StateFlow<List<com.example.database.UserAlarm>> = MutableStateFlow(emptyList())
    fun toggleAlarm(alarm: com.example.database.UserAlarm) {}
    fun deleteAlarm(alarm: com.example.database.UserAlarm) {}
    fun addAlarm(alarm: com.example.database.UserAlarm) {}
}
class TrackerViewModel : ViewModel() {
    val uiState: StateFlow<TrackerUiState> = MutableStateFlow(TrackerUiState())
    val history: StateFlow<List<com.example.database.DailyTracker>> = MutableStateFlow(emptyList())
    fun saveTracker(tracker: com.example.database.DailyTracker) {}
    fun saveTracker(context: android.content.Context, tracker: com.example.database.DailyTracker) {}
}
data class TrackerUiState(val history: List<com.example.database.DailyTracker> = emptyList())
data class District(val name: String, val englishName: String, val lat: Double, val lng: Double)
fun getDistrictsForCountry(countryCode: String): List<District> = emptyList()

data class AppLanguage(val code: String, val name: String)
object AppLanguages {
    const val BENGALI = "bn"
    const val ENGLISH = "en"
}
