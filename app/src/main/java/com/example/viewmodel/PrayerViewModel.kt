package com.example.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.PrayerCalculator
import com.example.calculator.PrayerTimes
import com.example.receiver.AlarmHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.util.Calendar
import java.util.TimeZone
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object GlobalLanguage {
    var isEnglish: Boolean = false
}

fun String.toBengali(): String {
    if (GlobalLanguage.isEnglish) {
        val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "এএম", "পিএম")
        val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "AM", "PM")
        var res = this
        ben.forEachIndexed { index, s ->
            res = res.replace(s, eng[index])
        }
        return res
    }
    val eng = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "AM", "PM", "am", "pm")
    val ben = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "এএম", "পিএম", "এএম", "পিএম")
    var res = this
    eng.forEachIndexed { index, s ->
        res = res.replace(s, ben[index])
    }
    return res
}

data class ViewState(
    val isLoading: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isAutoLocation: Boolean = true,
    val locationName: String = "ঢাকা",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val selectedCountry: String = "বাংলাদেশ",
    val selectedDistrict: String = "ঢাকা",
    val currentDate: String = "",
    val prayerTimes: PrayerTimes? = null,
    val nextPrayerName: String = "Loading...",
    val nextPrayerNameBen: String = "...",
    val nextPrayerRemaining: String = "০০:০০:০০",
    val timerProgress: Float = 0f,
    val specialCountdownLabel: String = "সাহরির বাকি",
    val specialCountdownTime: String = "০০:০০:০০",
    val specialCountdownProgress: Float = 0f,
    val alarms: Map<String, Boolean> = mapOf(
        "Fajr" to true,
        "Sunrise" to false,
        "Dhuhr" to true,
        "Asr" to true,
        "Maghrib" to true,
        "Isha" to true
    ),
    val forbiddenSunrise: String = "০০:০০",
    val forbiddenSunriseEnd: String = "০০:০০",
    val forbiddenNoon: String = "০০:০০",
    val forbiddenNoonEnd: String = "০০:০০",
    val forbiddenSunset: String = "০০:০০",
    val forbiddenSunsetEnd: String = "০০:০০",
    val currentHourDecimal: Double = 0.0,
    val isRainy: Boolean = false,
    val currentPrayerName: String = "",
    val currentPrayerNameBen: String = "",
    val rotatingNames: List<String> = emptyList(),
    val madhab: Int = 2, // 1 for Shafi, 2 for Hanafi
    val error: String? = null,
    val fajrCountdown: String = "",
    val dhuhrCountdown: String = "",
    val asrCountdown: String = "",
    val maghribCountdown: String = "",
    val ishaCountdown: String = "",
    val sehriCountdown: String = "",
    val iftarCountdown: String = "",
    val sunriseCountdown: String = "",
    val sunsetCountdown: String = "",
    val forbiddenSunriseCountdown: String = "",
    val forbiddenNoonCountdown: String = "",
    val forbiddenSunsetCountdown: String = "",
    val isIftarCountdown: Boolean = false
)

class PrayerViewModel : ViewModel() {
    private val _state = MutableStateFlow(ViewState())
    val state: StateFlow<ViewState> = _state.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var timerJob: Job? = null

    private var lastLat = 23.8103
    private var lastLng = 90.4125
    private var lastOffset = 6.0
    private var lastMadhab = 2
    private var hasLocationData = true

    init {
        // We will receive context later to load from prefs if needed, 
        // initially use defaults
        refreshState()
    }
    
    fun setMadhab(context: Context, m: Int) {
        lastMadhab = m
        context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE).edit().putInt("madhab", m).apply()
        _state.update { it.copy(madhab = m) }
        refreshState()
        com.example.widget.WidgetUtils.updateAllWidgets(context)
    }
    
    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        lastMadhab = prefs.getInt("madhab", 2)
        
        val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        val loadedAlarms = mutableMapOf<String, Boolean>()
        listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { name ->
            loadedAlarms[name] = alarmPrefs.getBoolean("alarm_$name", name != "Sunrise")
        }

        val isAuto = alarmPrefs.getBoolean("is_auto_location", true)
        
        val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedCountryCode = settingsPrefs.getString("selected_country_code", "BD") ?: "BD"
        
        var savedDist = alarmPrefs.getString("saved_district", "ঢাকা") ?: "ঢাকা"
        lastLat = alarmPrefs.getFloat("lat", 23.8103f).toDouble()
        lastLng = alarmPrefs.getFloat("lng", 90.4125f).toDouble()
        lastOffset = alarmPrefs.getFloat("offset", 6.0f).toDouble()
        
        // Ensure savedDist is from the current country if not auto location
        if (!isAuto) {
            val validDistricts = com.example.viewmodel.getDistrictsForCountry(savedCountryCode)
            val isValid = validDistricts.any { it.name == savedDist || it.englishName == savedDist }
            if (!isValid && validDistricts.isNotEmpty()) {
                val fallback = validDistricts.first()
                savedDist = fallback.name
                lastLat = fallback.lat
                lastLng = fallback.lng
                alarmPrefs.edit()
                    .putString("saved_district", savedDist)
                    .putFloat("lat", lastLat.toFloat())
                    .putFloat("lng", lastLng.toFloat())
                    .apply()
            }
        }
        
        hasLocationData = true

        _state.update { 
            it.copy(
                madhab = lastMadhab,
                alarms = loadedAlarms,
                isAutoLocation = isAuto,
                locationName = if (isAuto) "আমার অবস্থান" else savedDist,
                selectedDistrict = savedDist,
                selectedCountry = savedCountryCode,
                latitude = lastLat,
                longitude = lastLng
            ) 
        }
        refreshState()
        if (isAuto) {
            startLocationUpdates(context)
        }
    }

    private fun refreshState() {
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.US)
        val defaultTimes = PrayerCalculator.calculatePrayerTimes(lastLat, lastLng, lastOffset, lastMadhab)
        _state.update {
            it.copy(
                isLoading = false,
                currentDate = dateFormat.format(Date()).toBengali(),
                prayerTimes = defaultTimes,
                latitude = lastLat,
                longitude = lastLng
            )
        }
        calculateForbiddenTimes(defaultTimes)
        updateNextPrayer(defaultTimes)
    }

    private fun calculateForbiddenTimes(times: com.example.calculator.PrayerTimes) {
        val format = { h: Double ->
            val totalMin = (h * 60).toInt()
            val hour = (totalMin / 60) % 24
            val min = totalMin % 60
            val p = if (hour >= 12) "পিএম" else "এএম"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            String.format("%02d:%02d %s", displayHour, min, p).toBengali()
        }

        _state.update {
            it.copy(
                forbiddenSunrise = format(times.sunriseHours),
                forbiddenSunriseEnd = format(times.sunriseHours + 15.0 / 60.0),
                forbiddenNoon = format(times.dhuhrHours - 15.0 / 60.0),
                forbiddenNoonEnd = format(times.dhuhrHours),
                forbiddenSunset = format(times.maghribHours - 15.0 / 60.0),
                forbiddenSunsetEnd = format(times.maghribHours)
            )
        }
    }

    fun setLocationManually(context: Context, districtName: String, lat: Double, lng: Double) {
        lastLat = lat
        lastLng = lng
        val calculatedOffset = Math.round(lng / 15.0).toDouble()
        lastOffset = calculatedOffset
        
        hasLocationData = true
        _state.update { 
            it.copy(
                isAutoLocation = false,
                locationName = districtName,
                selectedDistrict = districtName,
                latitude = lat,
                longitude = lng
            ) 
        }
        val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        alarmPrefs.edit()
            .putBoolean("is_auto_location", false)
            .putString("saved_district", districtName)
            .putFloat("lat", lat.toFloat())
            .putFloat("lng", lng.toFloat())
            .putFloat("offset", lastOffset.toFloat())
            .apply()
            
        refreshState()
        AlarmHelper.scheduleNextPrayer(
            context = context, 
            lat = lastLat, 
            lng = lastLng, 
            timezoneOffsetHor = lastOffset, 
            alarms = _state.value.alarms,
            locationName = districtName,
            isAuto = false
        )
        com.example.widget.WidgetUtils.updateAllWidgets(context)
    }

    fun setAutoLocation(context: Context) {
        setAutoLocationEnabled(context, true)
    }

    fun setAutoLocationEnabled(context: Context, enabled: Boolean) {
        _state.update { it.copy(isAutoLocation = enabled) }
        val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
        alarmPrefs.edit().putBoolean("is_auto_location", enabled).apply()
        if (enabled) {
            startLocationUpdates(context)
        } else {
            locationCallback?.let {
                fusedLocationClient?.removeLocationUpdates(it)
                locationCallback = null
            }
            val savedDist = alarmPrefs.getString("saved_district", "Dhaka") ?: "Dhaka"
            _state.update {
                it.copy(
                    locationName = savedDist,
                    selectedDistrict = savedDist
                )
            }
            refreshState()
        }
        com.example.widget.WidgetUtils.updateAllWidgets(context)
    }

    fun forceRefreshLocation(context: Context) {
        setAutoLocationEnabled(context, true)
    }

    fun toggleAlarm(context: Context, prayerName: String) {
        _state.update { current ->
            val newAlarms = current.alarms.toMutableMap()
            newAlarms[prayerName] = !(newAlarms[prayerName] ?: true)
            current.copy(alarms = newAlarms)
        }
        if (hasLocationData) {
            AlarmHelper.scheduleNextPrayer(
                context = context, 
                lat = lastLat, 
                lng = lastLng, 
                timezoneOffsetHor = lastOffset, 
                alarms = _state.value.alarms,
                locationName = if (_state.value.isAutoLocation) "আমার অবস্থান" else _state.value.selectedDistrict,
                isAuto = _state.value.isAutoLocation
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (!_state.value.isAutoLocation) return

        val fineLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            coarseLocationPermission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _state.update { it.copy(hasLocationPermission = false, isLoading = false, error = "Permission Required") }
            return
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
            locationCallback = null
        }

        _state.update { it.copy(hasLocationPermission = true) }

        // Fetch standard LocationManager known location
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        var lastKnownLoc: android.location.Location? = null
        if (locationManager != null) {
            try {
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (lastKnownLoc == null || loc.time > lastKnownLoc.time) {
                            lastKnownLoc = loc
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Process resolved location helper
        fun processResolvedLocation(location: android.location.Location) {
            if (!_state.value.isAutoLocation) return
            _state.update { it.copy(isLoading = true) }
            
            viewModelScope.launch(Dispatchers.IO) {
                val calculatedOffset = Math.round(location.longitude / 15.0).toDouble()
                lastLat = location.latitude
                lastLng = location.longitude
                lastOffset = calculatedOffset
                hasLocationData = true

                // Try to resolve city name using Geocoder on background
                var resolvedCityName = if (GlobalLanguage.isEnglish) "My Location" else "আমার অবস্থান"
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        resolvedCityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: address.featureName ?: resolvedCityName
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
                alarmPrefs.edit()
                    .putFloat("lat", lastLat.toFloat())
                    .putFloat("lng", lastLng.toFloat())
                    .putFloat("offset", lastOffset.toFloat())
                    .putString("saved_district", resolvedCityName)
                    .apply()

                val times = PrayerCalculator.calculatePrayerTimes(lastLat, lastLng, lastOffset, lastMadhab)
                
                withContext(Dispatchers.Main) {
                    calculateForbiddenTimes(times)
                    AlarmHelper.scheduleNextPrayer(
                        context = context, 
                        lat = lastLat, 
                        lng = lastLng, 
                        timezoneOffsetHor = lastOffset, 
                        alarms = _state.value.alarms,
                        locationName = resolvedCityName,
                        isAuto = true
                    )

                    _state.update { 
                        it.copy(
                            isLoading = false,
                            prayerTimes = times, 
                            locationName = resolvedCityName, 
                            selectedDistrict = resolvedCityName, 
                            latitude = lastLat, 
                            longitude = lastLng
                        ) 
                    }
                    updateNextPrayer(times)
                    com.example.widget.WidgetUtils.updateAllWidgets(context)
                }
            }
        }

        // If we have a very fresh location from LocationManager, prioritize it
        if (lastKnownLoc != null && (System.currentTimeMillis() - lastKnownLoc.time) < 5 * 60 * 1000) {
            processResolvedLocation(lastKnownLoc)
            return
        }

        // Query FusedLocationProviderClient first as it can be accurate, falling back immediately to LocationManager
        try {
            fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                ?.addOnSuccessListener { location ->
                    if (location != null) {
                        processResolvedLocation(location)
                    } else if (lastKnownLoc != null) {
                        processResolvedLocation(lastKnownLoc)
                    } else {
                        // Fallback to LocationCallback to actively fetch location
                        try {
                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                                .setMaxUpdates(1)
                                .build()
                            val newCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult) {
                                    result.lastLocation?.let {
                                        processResolvedLocation(it)
                                    }
                                    fusedLocationClient?.removeLocationUpdates(this)
                                }
                            }
                            locationCallback = newCallback
                            fusedLocationClient?.requestLocationUpdates(locationRequest, newCallback, Looper.getMainLooper())
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }?.addOnFailureListener {
                    if (lastKnownLoc != null) {
                        processResolvedLocation(lastKnownLoc)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            if (lastKnownLoc != null) {
                processResolvedLocation(lastKnownLoc)
            }
        }
    }
    
    fun detectLocationViaWeb(context: Context) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://ipapi.co/json/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    
                    val json = JSONObject(response.toString())
                    val city = json.optString("city", if (GlobalLanguage.isEnglish) "Detected City" else "সনাক্তকৃত শহর")
                    val lat = json.getDouble("latitude")
                    val lng = json.getDouble("longitude")
                    val calculatedOffset = Math.round(lng / 15.0).toDouble()
                    
                    lastLat = lat
                    lastLng = lng
                    lastOffset = calculatedOffset
                    hasLocationData = true
                    
                    val alarmPrefs = context.getSharedPreferences("prayer_alarm_prefs", Context.MODE_PRIVATE)
                    alarmPrefs.edit()
                        .putBoolean("is_auto_location", true)
                        .putString("saved_district", city)
                        .putFloat("lat", lat.toFloat())
                        .putFloat("lng", lng.toFloat())
                        .putFloat("offset", calculatedOffset.toFloat())
                        .apply()
                        
                    val times = PrayerCalculator.calculatePrayerTimes(lat, lng, calculatedOffset, lastMadhab)
                    
                    withContext(Dispatchers.Main) {
                        calculateForbiddenTimes(times)
                        AlarmHelper.scheduleNextPrayer(
                            context = context, 
                            lat = lat, 
                            lng = lng, 
                            timezoneOffsetHor = calculatedOffset, 
                            alarms = _state.value.alarms,
                            locationName = city,
                            isAuto = true
                        )
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isAutoLocation = true,
                                locationName = city,
                                selectedDistrict = city,
                                latitude = lat,
                                longitude = lng,
                                prayerTimes = times
                            ) 
                        }
                        updateNextPrayer(times)
                        com.example.widget.WidgetUtils.updateAllWidgets(context)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(isLoading = false, error = "HTTP error: $responseCode") }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
                }
            }
        }
    }
    
    fun setPermissionDenied() {
        _state.update { it.copy(hasLocationPermission = false, isLoading = false, error = "Permission Required") }
    }

    private fun updateNextPrayer(times: PrayerTimes) {
        startCountdownTimer()
    }

    private fun formatDiff(diffInHours: Double, isEnglish: Boolean): String {
        var d = diffInHours
        if (d.isNaN() || d.isInfinite()) {
            return if (isEnglish) "00:00:00" else "০০:০০:০০"
        }
        d = d % 24.0
        if (d < 0) d += 24.0
        val totalSeconds = Math.round(d * 3600).toInt()
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        val str = String.format("%02d:%02d:%02d", h, m, s)
        return if (isEnglish) str else str.toBengali()
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            android.util.Log.d("PrayerViewModel", "startCountdownTimer job started successfully")
            while(true) {
                try {
                    val times = _state.value.prayerTimes ?: PrayerCalculator.calculatePrayerTimes(lastLat, lastLng, lastOffset, lastMadhab)
                    val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                    var currentHourDec = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE)/60.0 + cal.get(Calendar.SECOND)/3600.0 + lastOffset
                    while (currentHourDec < 0) currentHourDec += 24.0
                    while (currentHourDec >= 24) currentHourDec -= 24.0
                    
                    val sunriseHours = times.sunriseHours
                    val dhuhrHours = times.dhuhrHours
                    val asrHours = times.asrHours
                    val maghribHours = times.maghribHours
                    val ishaHours = times.ishaHours
                    val fajrHours = times.fajrHours

                    // Check if we are in the Nafl/Chasht period (Sunrise to Dhuhr)
                    val isNaflPeriod = currentHourDec >= sunriseHours && currentHourDec < dhuhrHours

                    var currentName = ""
                    var currentNameBen = ""
                    var currentStartTime = 0.0
                    var currentEndTime = 0.0

                    var nextName = ""
                    var nextNameBen = ""

                    val isEng = GlobalLanguage.isEnglish

                    if (isNaflPeriod) {
                        currentName = "Duha"
                        currentNameBen = if (isEng) "Chasht" else "চাশত"
                        currentStartTime = sunriseHours
                        currentEndTime = dhuhrHours

                        nextName = "Dhuhr"
                        nextNameBen = if (isEng) "Dhuhr" else "যোহর"
                    } else {
                        if (currentHourDec >= fajrHours && currentHourDec < sunriseHours) {
                            currentName = "Fajr"
                            currentNameBen = if (isEng) "Fajr" else "ফজর"
                            currentStartTime = fajrHours
                            currentEndTime = sunriseHours

                            nextName = "Dhuhr"
                            nextNameBen = if (isEng) "Dhuhr" else "যোহর"
                        } else if (currentHourDec >= dhuhrHours && currentHourDec < asrHours) {
                            currentName = "Dhuhr"
                            currentNameBen = if (isEng) "Dhuhr" else "যোহর"
                            currentStartTime = dhuhrHours
                            currentEndTime = asrHours

                            nextName = "Asr"
                            nextNameBen = if (isEng) "Asr" else "আসর"
                        } else if (currentHourDec >= asrHours && currentHourDec < maghribHours) {
                            currentName = "Asr"
                            currentNameBen = if (isEng) "Asr" else "আসর"
                            currentStartTime = asrHours
                            currentEndTime = maghribHours

                            nextName = "Maghrib"
                            nextNameBen = if (isEng) "Maghrib" else "মাগরিব"
                        } else if (currentHourDec >= maghribHours && currentHourDec < ishaHours) {
                            currentName = "Maghrib"
                            currentNameBen = if (isEng) "Maghrib" else "মাগরিব"
                            currentStartTime = maghribHours
                            currentEndTime = ishaHours

                            nextName = "Isha"
                            nextNameBen = if (isEng) "Isha" else "এশা"
                        } else {
                            currentName = "Isha"
                            currentNameBen = if (isEng) "Isha" else "এশা"
                            
                            if (currentHourDec >= ishaHours) {
                                currentStartTime = ishaHours
                                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                val tomorrowTimes = PrayerCalculator.calculatePrayerTimes(lastLat, lastLng, lastOffset, lastMadhab, tomorrow)
                                currentEndTime = tomorrowTimes.fajrHours + 24.0
                            } else {
                                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                                val yesterdayTimes = PrayerCalculator.calculatePrayerTimes(lastLat, lastLng, lastOffset, lastMadhab, yesterday)
                                currentStartTime = yesterdayTimes.ishaHours - 24.0
                                currentEndTime = fajrHours
                            }

                            nextName = "Fajr"
                            nextNameBen = if (isEng) "Fajr" else "ফজর"
                        }
                    }

                    // Handle rotating names for Nafl period (Between Sunrise and Dhuhr)
                    val rotating = if (isNaflPeriod) {
                        if (isEng) listOf("Ishraq", "Chasht", "Duha") 
                        else listOf("ইশরাক", "চাশত", "দোহা")
                    } else {
                        emptyList()
                    }

                    // Next Prayer Countdown
                    var diff = currentEndTime - currentHourDec
                    if (diff < 0) diff += 24.0
                    val totalDuration = currentEndTime - currentStartTime
                    val progress = if (totalDuration <= 0.0 || totalDuration.isNaN() || totalDuration.isInfinite()) {
                        0f
                    } else {
                        val res = ((currentHourDec - currentStartTime) / totalDuration)
                        if (res.isNaN() || res.isInfinite()) 0f else res.coerceIn(0.0, 1.0).toFloat()
                    }
                    val nextPrayerRemainingVal = formatDiff(diff, isEng)

                    // Sehri / Iftar Countdown
                    val fajr = times.fajrHours
                    val maghrib = times.maghribHours
                    
                    var specialLabel = if (isEng) "Sehri Remaining" else "সাহরির বাকি"
                    var targetSpecialHour = fajr
                    var startSpecialHour = maghrib - 24.0
                    var isIftarCountdownVal = false

                    if (currentHourDec > fajr && currentHourDec < maghrib) {
                        specialLabel = if (isEng) "Iftar Remaining" else "ইফতারের বাকি"
                        targetSpecialHour = maghrib
                        startSpecialHour = fajr
                        isIftarCountdownVal = true
                    } else {
                        specialLabel = if (isEng) "Sehri Remaining" else "সাহরির বাকি"
                        targetSpecialHour = if (currentHourDec > maghrib) fajr + 24.0 else fajr
                        startSpecialHour = if (currentHourDec > maghrib) maghrib else maghrib - 24.0
                        isIftarCountdownVal = false
                    }

                    val specDiff = targetSpecialHour - currentHourDec
                    val specTotal = targetSpecialHour - startSpecialHour
                    val specProgress = if (specTotal <= 0.0 || specTotal.isNaN() || specTotal.isInfinite()) {
                        0f
                    } else {
                        val res = ((currentHourDec - startSpecialHour) / specTotal)
                        if (res.isNaN() || res.isInfinite()) 0f else res.coerceIn(0.0, 1.0).toFloat()
                    }
                    val specTimeStr = formatDiff(specDiff, isEng)

                    // Real-time Countdown Calculations for ALL items requested by user:
                    // 1. Fajr
                    val fajrActive = currentHourDec >= times.fajrHours && currentHourDec < times.sunriseHours
                    val fajrCountdownVal = if (fajrActive) {
                        val label = if (isEng) "Ends: " else "শেষ: "
                        label + formatDiff(times.sunriseHours - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts: " else "শুরু: "
                        var d = times.fajrHours - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 2. Dhuhr
                    val dhuhrActive = currentHourDec >= times.dhuhrHours && currentHourDec < times.asrHours
                    val dhuhrCountdownVal = if (dhuhrActive) {
                        val label = if (isEng) "Ends: " else "শেষ: "
                        label + formatDiff(times.asrHours - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts: " else "শুরু: "
                        var d = times.dhuhrHours - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 3. Asr
                    val asrActive = currentHourDec >= times.asrHours && currentHourDec < times.maghribHours
                    val asrCountdownVal = if (asrActive) {
                        val label = if (isEng) "Ends: " else "শেষ: "
                        label + formatDiff(times.maghribHours - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts: " else "শুরু: "
                        var d = times.asrHours - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 4. Maghrib / Iftar
                    val maghribActive = currentHourDec >= times.maghribHours && currentHourDec < times.ishaHours
                    val maghribCountdownVal = if (maghribActive) {
                        val label = if (isEng) "Ends: " else "শেষ: "
                        label + formatDiff(times.ishaHours - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts: " else "শুরু: "
                        var d = times.maghribHours - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 5. Isha
                    val ishaActive = currentHourDec >= times.ishaHours || currentHourDec < times.fajrHours
                    val ishaCountdownVal = if (ishaActive) {
                        val tomorrowFajr = if (currentHourDec >= times.ishaHours) times.fajrHours + 24.0 else times.fajrHours
                        val label = if (isEng) "Ends: " else "শেষ: "
                        label + formatDiff(tomorrowFajr - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts: " else "শুরু: "
                        var d = times.ishaHours - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 6. Sehri Time (Starts at Sunset, ends at Fajr)
                    var sehriDiff = times.fajrHours - currentHourDec
                    if (sehriDiff < 0) sehriDiff += 24.0
                    val sehriCountdownVal = formatDiff(sehriDiff, isEng)

                    // 7. Iftar Time (Starts at Maghrib)
                    var iftarDiff = times.maghribHours - currentHourDec
                    if (iftarDiff < 0) iftarDiff += 24.0
                    val iftarCountdownVal = formatDiff(iftarDiff, isEng)

                    // 8. Sunrise Countdown
                    var sunriseDiff = times.sunriseHours - currentHourDec
                    if (sunriseDiff < 0) sunriseDiff += 24.0
                    val sunriseCountdownVal = formatDiff(sunriseDiff, isEng)

                    // 9. Sunset Countdown
                    var sunsetDiff = times.maghribHours - currentHourDec
                    if (sunsetDiff < 0) sunsetDiff += 24.0
                    val sunsetCountdownVal = formatDiff(sunsetDiff, isEng)

                    // 10. Forbidden Sunrise Period (Sunrise to Sunrise + 15 mins)
                    val fSunriseStart = times.sunriseHours
                    val fSunriseEnd = times.sunriseHours + 15.0 / 60.0
                    val fSunriseActive = currentHourDec >= fSunriseStart && currentHourDec < fSunriseEnd
                    val fSunriseCountdownVal = if (fSunriseActive) {
                        val label = if (isEng) "Ends in " else "শেষ হতে "
                        label + formatDiff(fSunriseEnd - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts in " else "শুরু হতে "
                        var d = fSunriseStart - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 11. Forbidden Noon Period (Dhuhr - 15 mins to Dhuhr)
                    val fNoonStart = times.dhuhrHours - 15.0 / 60.0
                    val fNoonEnd = times.dhuhrHours
                    val fNoonActive = currentHourDec >= fNoonStart && currentHourDec < fNoonEnd
                    val fNoonCountdownVal = if (fNoonActive) {
                        val label = if (isEng) "Ends in " else "শেষ হতে "
                        label + formatDiff(fNoonEnd - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts in " else "শুরু হতে "
                        var d = fNoonStart - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    // 12. Forbidden Sunset Period (Maghrib - 15 mins to Maghrib)
                    val fSunsetStart = times.maghribHours - 15.0 / 60.0
                    val fSunsetEnd = times.maghribHours
                    val fSunsetActive = currentHourDec >= fSunsetStart && currentHourDec < fSunsetEnd
                    val fSunsetCountdownVal = if (fSunsetActive) {
                        val label = if (isEng) "Ends in " else "শেষ হতে "
                        label + formatDiff(fSunsetEnd - currentHourDec, isEng)
                    } else {
                        val label = if (isEng) "Starts in " else "শুরু হতে "
                        var d = fSunsetStart - currentHourDec
                        if (d < 0) d += 24.0
                        label + formatDiff(d, isEng)
                    }

                    withContext(Dispatchers.Main) {
                        _state.update { 
                            it.copy(
                                currentHourDecimal = currentHourDec,
                                currentPrayerName = currentName, 
                                currentPrayerNameBen = currentNameBen,
                                nextPrayerName = nextName,
                                nextPrayerNameBen = nextNameBen,
                                rotatingNames = rotating,
                                nextPrayerRemaining = nextPrayerRemainingVal,
                                timerProgress = progress,
                                specialCountdownLabel = specialLabel,
                                specialCountdownTime = specTimeStr,
                                specialCountdownProgress = specProgress,
                                fajrCountdown = fajrCountdownVal,
                                dhuhrCountdown = dhuhrCountdownVal,
                                asrCountdown = asrCountdownVal,
                                maghribCountdown = maghribCountdownVal,
                                ishaCountdown = ishaCountdownVal,
                                sehriCountdown = sehriCountdownVal,
                                iftarCountdown = iftarCountdownVal,
                                sunriseCountdown = sunriseCountdownVal,
                                sunsetCountdown = sunsetCountdownVal,
                                forbiddenSunriseCountdown = fSunriseCountdownVal,
                                forbiddenNoonCountdown = fNoonCountdownVal,
                                forbiddenSunsetCountdown = fSunsetCountdownVal,
                                isIftarCountdown = isIftarCountdownVal
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PrayerViewModel", "Error in countdown loop", e)
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        timerJob?.cancel()
    }
}
