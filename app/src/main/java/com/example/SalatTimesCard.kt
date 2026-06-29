package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.ViewState
import com.example.viewmodel.toBengali

@Composable
fun SalatTimesCard(state: ViewState) {
    state.prayerTimes?.let { times ->
        val isEng = GlobalLanguage.isEnglish

        // Formatting Helpers
        val formatTime = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val p = if (hour >= 12) {
                if (isEng) "PM" else "পি.এম"
            } else {
                if (isEng) "AM" else "এ.এম"
            }
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d %s", displayHour, min, p)
            if (isEng) timeStr else timeStr.toBengali()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) 
            ) {
                // Left Column: Nafil
                Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp, horizontal = 8.dp)) {
                    Text(
                        text = if (isEng) "Nafil Salat" else "নফল নামাজ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    OrnamentalDivider()
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Tahajjud" else "তাহাজ্জুদ",
                        time = formatTime(times.fajrHours - 1.2),
                        isActive = false
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.MenuBook,
                        name = if (isEng) "Ishraq" else "ইশরাক",
                        time = formatTime(times.sunriseHours + 0.3),
                        isActive = false
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Chasht" else "চাশত",
                        time = formatTime(times.sunriseHours + 1.5),
                        isActive = false
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Awwabin" else "আওয়াবীন",
                        time = formatTime(times.maghribHours + 0.3),
                        isActive = false
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(0.6.dp)
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                        .background(Color(0xFFEEEEEE))
                )

                // Right Column: Farz
                Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp, horizontal = 8.dp)) {
                    Text(
                        text = if (isEng) "Fard Prayers" else "ফরজ নামাজ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    OrnamentalDivider()
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Fajr" else "ফজর",
                        time = formatTime(times.fajrHours),
                        isActive = state.currentPrayerName == "Fajr",
                        countdown = state.fajrCountdown
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Dhuhr" else "যোহর",
                        time = formatTime(times.dhuhrHours),
                        isActive = state.currentPrayerName == "Dhuhr",
                        countdown = state.dhuhrCountdown
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.Cloud,
                        name = if (isEng) "Asr" else "আসর",
                        time = formatTime(times.asrHours),
                        isActive = state.currentPrayerName == "Asr",
                        countdown = state.asrCountdown
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Maghrib" else "মাগরিব",
                        time = formatTime(times.maghribHours),
                        isActive = state.currentPrayerName == "Maghrib",
                        countdown = state.maghribCountdown
                    )
                    SalatRowCompact(
                        icon = Icons.Outlined.ModeNight,
                        name = if (isEng) "Isha" else "এশা",
                        time = formatTime(times.ishaHours),
                        isActive = state.currentPrayerName == "Isha",
                        countdown = state.ishaCountdown
                    )
                }
            }
        }
    }
}

@Composable
fun OrnamentalDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f).height(0.6.dp).background(Color(0xFFEEEEEE)))
    }
}

@Composable
fun CustomHorizontalDivider() {
    HorizontalDivider(
        color = Color(0xFFF5F5F5),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
fun SalatRowCompact(icon: ImageVector, name: String, time: String, isActive: Boolean, countdown: String = "") {
    val textColor = if (isActive) Color(0xFF10B982) else Color(0xFF444444)
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
    val bgColor = if (isActive) Color(0xFF10B982).copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color(0xFF10B982) else Color(0xFF757575),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 10.5.sp,
                fontWeight = fontWeight,
                color = textColor,
                maxLines = 1
            )
            if (countdown.isNotEmpty()) {
                Text(
                    text = countdown,
                    fontSize = 8.sp,
                    color = if (isActive) Color(0xFF10B982) else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF10B982) else Color(0xFF212121),
            maxLines = 1
        )
    }
}
