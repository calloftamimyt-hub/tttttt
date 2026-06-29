package com.example.calculator

class PrayerTimes(
    val fajr: String = "",
    val sunrise: String = "",
    val dhuhr: String = "",
    val asr: String = "",
    val maghrib: String = "",
    val isha: String = ""
)

object PrayerCalculator {
    fun calculate(date: java.util.Date, lat: Double, lng: Double, timezone: Double): Any = Any()
}
