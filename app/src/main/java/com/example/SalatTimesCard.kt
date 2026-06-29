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
            val normalizedSeconds = if (totalSeconds < 0) totalSeconds + 24 * 3600 else totalSeconds
            val hour = (normalizedSeconds / 3600) % 24
            val min = (normalizedSeconds / 60) % 60
            val p = if (hour >= 12) (if(isEng) "PM" else "PM") else (if(isEng) "AM" else "AM")
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            String.format("%02d:%02d %s", displayHour, min, p).toBengali()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) 
            ) {
                // Left Column: Nafal
                Column(modifier = Modifier.weight(1f).padding(top = 12.dp, bottom = 12.dp, start = 10.dp, end = 6.dp)) {
                    Text(
                        text = if (isEng) "Nafil Salat" else "নফল সালাত",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    OrnamentalDivider()
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Tahajjud" else "তাহাজ্জুদ",
                        time = formatTime(times.fajrHours - 1.25),
                        isActive = false
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.MenuBook,
                        name = if (isEng) "Ishraq" else "ইশরাক",
                        time = formatTime(times.sunriseHours + 0.25),
                        isActive = state.currentPrayerName == "Duha"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Chasht" else "চাশত",
                        time = formatTime(times.sunriseHours + 1.5),
                        isActive = state.currentPrayerName == "Duha"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Awwabin" else "আওয়াবীন",
                        time = formatTime(times.maghribHours + 0.25),
                        isActive = false
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.ModeNight,
                        name = if (isEng) "Tahajjud (Night)" else "তাহাজ্জুদ (রাত)",
                        time = formatTime(times.ishaHours + 1.5),
                        isActive = false
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(0.8.dp)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFEEEEEE))
                )

                // Right Column: Farz
                Column(modifier = Modifier.weight(1f).padding(top = 12.dp, bottom = 12.dp, start = 6.dp, end = 10.dp)) {
                    Text(
                        text = if (isEng) "Fard Prayers" else "ফরজ নামাজ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    OrnamentalDivider()
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Fajr" else "ফজর",
                        time = times.fajr.toBengali(),
                        isActive = state.currentPrayerName == "Fajr"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Dhuhr" else "যোহর",
                        time = times.dhuhr.toBengali(),
                        isActive = state.currentPrayerName == "Dhuhr"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.Cloud,
                        name = if (isEng) "Asr" else "আসর",
                        time = times.asr.toBengali(),
                        isActive = state.currentPrayerName == "Asr"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Maghrib" else "মাগরিব",
                        time = times.maghrib.toBengali(),
                        isActive = state.currentPrayerName == "Maghrib"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.ModeNight,
                        name = if (isEng) "Isha" else "এশা",
                        time = times.isha.toBengali(),
                        isActive = state.currentPrayerName == "Isha"
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
        Box(modifier = Modifier.weight(1f).height(0.5.dp).background(Color(0xFFEEEEEE)))
    }
}

@Composable
fun CustomHorizontalDivider() {
    HorizontalDivider(
        color = Color(0xFFF5F5F5),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun SalatRowCompact(icon: ImageVector, name: String, time: String, isActive: Boolean) {
    val textColor = if (isActive) Color(0xFF10B982) else Color(0xFF333333)
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
    val bgColor = if (isActive) Color(0xFF10B982).copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = name,
            fontSize = 10.5.sp,
            fontWeight = fontWeight,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = time,
            fontSize = 10.5.sp,
            fontWeight = fontWeight,
            color = textColor,
            maxLines = 1
        )
    }
}
