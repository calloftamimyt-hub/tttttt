package com.example

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.Service
import android.os.IBinder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.reflect.KProperty

operator fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T = this.value

fun <T> StateFlow<T>.collectAsState(): State<T> = mutableStateOf(this.value)

// Stubs for missing UI components
@Composable fun TrackerScreen() {}
@Composable fun TasbihScreen() {}
@Composable fun SavedDuasScreen() {}
@Composable fun SavedHadithsScreen() {}
@Composable fun SettingsScreen() {}
@Composable fun ZakatCalculatorScreen() {}

fun Int.toBengaliDigits(): String = this.toString()

class AppCore {
    fun initialize() {}
}

@Composable fun SocialMediaBlockerScreen() {}
@Composable fun WebsiteBlockerScreen() {}
@Composable fun ScreenTimeScreen() {}

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
    val parents: Boolean = false
)

object CountryData {
    val components: List<Country> = emptyList()
}
class Country(val name: String = "", val code: String = "", val flag: String = "")
class PrayerTimes(
    val fajr: String = "",
    val sunrise: String = "",
    val dhuhr: String = "",
    val asr: String = "",
    val maghrib: String = "",
    val isha: String = "",
    val fajrHours: Double = 0.0,
    val sunriseHours: Double = 0.0,
    val dhuhrHours: Double = 0.0,
    val asrHours: Double = 0.0,
    val maghribHours: Double = 0.0,
    val ishaHours: Double = 0.0
)

// NOTE: Kotlin does not allow multiple packages in one file with declarations before the package statement.
// Wait, I will just put them in com.example and then write `create_file` for other packages.
