package com.example.calculator

import java.util.Calendar
import java.util.Date
import kotlin.math.*

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

object PrayerCalculator {
    fun calculate(date: Date, lat: Double, lng: Double, timezone: Double): Any = Any()

    fun calculatePrayerTimes(
        lat: Double,
        lng: Double,
        offset: Double,
        madhab: Int,
        calendar: Calendar = Calendar.getInstance()
    ): PrayerTimes {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Calculate Solar Declination and Equation of Time (EqT)
        val b = 2 * PI * (dayOfYear - 81) / 365.0
        val eqt = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // in minutes
        val declination = 0.40928 * sin(2 * PI * (284 + dayOfYear) / 365.0) // Declination angle in radians
        
        val latRad = lat * PI / 180.0
        
        // Solar Noon (transit) in hours
        val noon = 12.0 + (offset - lng / 15.0) - (eqt / 60.0)
        
        // Sunrise/Sunset hour angle
        // standard altitude for sunrise/sunset is -0.833 degrees
        val sunriseAngleRad = -0.833 * PI / 180.0
        val cosH_sunrise = (sin(sunriseAngleRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_sunrise = if (cosH_sunrise in -1.0..1.0) acos(cosH_sunrise) * 180.0 / PI else 90.0
        
        val sunriseHours = noon - h_sunrise / 15.0
        val maghribHours = noon + h_sunrise / 15.0
        
        // Fajr hour angle (typically -18.0 degrees)
        val fajrAngleRad = -18.0 * PI / 180.0
        val cosH_fajr = (sin(fajrAngleRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_fajr = if (cosH_fajr in -1.0..1.0) acos(cosH_fajr) * 180.0 / PI else 110.0
        val fajrHours = noon - h_fajr / 15.0
        
        // Isha hour angle (typically -18.0 degrees)
        val ishaAngleRad = -18.0 * PI / 180.0
        val cosH_isha = (sin(ishaAngleRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_isha = if (cosH_isha in -1.0..1.0) acos(cosH_isha) * 180.0 / PI else 115.0
        val ishaHours = noon + h_isha / 15.0
        
        // Asr (Shafi shadow = 1, Hanafi shadow = 2)
        val shadowLength = if (madhab == 1) 1.0 else 2.0
        val acotVal = shadowLength + tan(abs(latRad - declination))
        val asrAngleRad = atan(1.0 / acotVal)
        val cosH_asr = (sin(asrAngleRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_asr = if (cosH_asr in -1.0..1.0) acos(cosH_asr) * 180.0 / PI else 45.0
        val asrHours = noon + h_asr / 15.0
        
        val dhuhrHours = noon + 4.0 / 60.0 // Add a 4 minute delay/safety offset for zenith
        
        val formatTime = { h: Double ->
            var hr = h
            while (hr < 0) hr += 24.0
            while (hr >= 24) hr -= 24.0
            val totalMinutes = Math.round(hr * 60).toInt()
            val hour24 = (totalMinutes / 60) % 24
            val minutePart = totalMinutes % 60
            val ampm = if (hour24 >= 12) "PM" else "AM"
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            String.format("%d:%02d %s", hour12, minutePart, ampm)
        }
        
        return PrayerTimes(
            fajr = formatTime(fajrHours),
            sunrise = formatTime(sunriseHours),
            dhuhr = formatTime(dhuhrHours),
            asr = formatTime(asrHours),
            maghrib = formatTime(maghribHours),
            isha = formatTime(ishaHours),
            fajrHours = fajrHours,
            sunriseHours = sunriseHours,
            dhuhrHours = dhuhrHours,
            asrHours = asrHours,
            maghribHours = maghribHours,
            ishaHours = ishaHours
        )
    }
}
