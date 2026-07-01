package com.example.calculator

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.*
import java.util.*

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
    val ishaHours: Double = 0.0,
    val sehri: String = "",
    val iftar: String = "",
    val sehriHours: Double = 0.0,
    val iftarHours: Double = 0.0
)

object PrayerCalculator {
    fun calculatePrayerTimes(
        lat: Double,
        lng: Double,
        offset: Double,
        madhab: Int,
        calendar: Calendar = Calendar.getInstance()
    ): PrayerTimes {
        val coordinates = Coordinates(lat, lng)
        val date = DateComponents.from(calendar.time)
        
        // Use Muslim World League as default, common for Bangladesh
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.madhab = if (madhab == 2) Madhab.HANAFI else Madhab.SHAFI
        
        val adhanTimes = com.batoulapps.adhan.PrayerTimes(coordinates, date, params)
        
        // Helper to convert Date to fractional hours in the requested timezone offset
        fun dateToHours(date: Date?): Double {
            if (date == null) return 0.0
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.time = date
            // UTC time + offset
            val utcHours = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0 + cal.get(Calendar.SECOND) / 3600.0
            var localHours = utcHours + offset
            while (localHours < 0) localHours += 24.0
            while (localHours >= 24) localHours -= 24.0
            return localHours
        }
        
        val formatTime = { h: Double ->
            val totalMinutes = Math.round(h * 60).toInt()
            val hour24 = (totalMinutes / 60) % 24
            val minutePart = totalMinutes % 60
            val ampm = if (hour24 >= 12) "PM" else "AM"
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            String.format("%d:%02d %s", hour12, minutePart, ampm)
        }
        
        val fH = dateToHours(adhanTimes.fajr)
        val sH = dateToHours(adhanTimes.sunrise)
        val dH = dateToHours(adhanTimes.dhuhr)
        val aH = dateToHours(adhanTimes.asr)
        val mH = dateToHours(adhanTimes.maghrib)
        val iH = dateToHours(adhanTimes.isha)
        
        return PrayerTimes(
            fajr = formatTime(fH),
            sunrise = formatTime(sH),
            dhuhr = formatTime(dH),
            asr = formatTime(aH),
            maghrib = formatTime(mH),
            isha = formatTime(iH),
            fajrHours = fH,
            sunriseHours = sH,
            dhuhrHours = dH,
            asrHours = aH,
            maghribHours = mH,
            ishaHours = iH,
            sehri = formatTime(fH - 3.0 / 60.0),
            iftar = formatTime(mH),
            sehriHours = fH - 3.0 / 60.0,
            iftarHours = mH
        )
    }
}
