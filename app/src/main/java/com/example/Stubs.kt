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
@Composable fun TasbihScreen(onBack: () -> Unit = {}) {}
@Composable fun SavedHadithsScreen(onBack: () -> Unit = {}) {}
@Composable fun ZakatCalculatorScreen(onBack: () -> Unit = {}) {}
@Composable fun AllahNamesScreen(onBack: () -> Unit = {}) {}

fun Int.toBengaliDigits(): String = this.toString()

class AppCore {
    fun initialize() {}
}

@Composable fun ScreenTimeScreen(onBack: () -> Unit = {}) {}

object CountryData {
    val components: List<Country> = emptyList()
}
class Country(val name: String = "", val code: String = "", val flag: String = "")
// Removed duplicate PrayerTimes to avoid conflict with com.example.calculator.PrayerTimes

// NOTE: Kotlin does not allow multiple packages in one file with declarations before the package statement.
// Wait, I will just put them in com.example and then write `create_file` for other packages.
