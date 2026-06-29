package com.example.ui

val LocalAppStrings = androidx.compose.runtime.staticCompositionLocalOf<AppStrings> { AppStrings() }

class AppStrings {
    val forbidden_times = ""
    val sunrise = ""
    val noon = ""
    val sunset = ""
    val settings = ""
    fun getString(id: String): String = ""
}
