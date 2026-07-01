package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

var isDarkModeGlobal by mutableStateOf(false)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val PrimaryGreen = Color(0xFF10B981)

val TextDark: Color
    get() = if (isDarkModeGlobal) Color(0xFFF1F5F9) else Color(0xFF1E293B)

val TextGray: Color
    get() = if (isDarkModeGlobal) Color(0xFF94A3B8) else Color(0xFF64748B)

val BgLight: Color
    get() = if (isDarkModeGlobal) Color(0xFF0F172A) else Color(0xFFF8FAFC)

val CardBg: Color
    get() = if (isDarkModeGlobal) Color(0xFF1E293B) else Color(0xFFFFFFFF)
