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
            val totalMin = (h * 60).toInt()
            val hour = (totalMin / 60) % 24
            val min = totalMin % 60
            val p = if (hour >= 12) (if(isEng) "PM" else "PM") else (if(isEng) "AM" else "AM")
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            String.format("%02d:%02d %s", displayHour, min, p).toBengali()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // This ensures the vertical divider takes full height
            ) {
                // Left Column: Nafal
                Column(modifier = Modifier.weight(1f).padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 8.dp)) {
                    Text(
                        text = if (isEng) "Nafl Salat Times" else "নফল সালাতের ওয়াক্ত",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    OrnamentalDivider()
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Tahajjud" else "তাহাজ্জুদ সালাত",
                        time = formatTime(times.fajrHours - 1.25),
                        isActive = false
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.MenuBook,
                        name = if (isEng) "Ishraq" else "ইশরাক সালাত",
                        time = formatTime(times.sunriseHours + 0.25),
                        isActive = state.currentPrayerName == "Duha"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Chasht" else "চাশত সালাত",
                        time = formatTime(times.sunriseHours + 1.5),
                        isActive = state.currentPrayerName == "Duha"
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Awwabin" else "আওয়াবীন সালাত",
                        time = formatTime(times.maghribHours + 0.25),
                        isActive = false
                    )
                    CustomHorizontalDivider()
                    SalatRowCompact(
                        icon = Icons.Outlined.ModeNight,
                        name = if (isEng) "Tahajjud (Night)" else "তাহাজ্জুদ সালাত (রাত)",
                        time = formatTime(times.ishaHours + 1.5),
                        isActive = false
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Right Column: Farz
                Column(modifier = Modifier.weight(1f).padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 12.dp)) {
                    Text(
                        text = if (isEng) "5 Waqt Prayers" else "পাঁচ ওয়াক্তের নামাজের সময়",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    OrnamentalDivider()
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE0E0E0)))
        Icon(
            imageVector = Icons.Outlined.Brightness1,
            contentDescription = null,
            tint = Color(0xFFE0E0E0),
            modifier = Modifier.size(6.dp).padding(horizontal = 2.dp)
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE0E0E0)))
    }
}

@Composable
fun CustomHorizontalDivider() {
    HorizontalDivider(
        color = Color(0xFFE0E0E0),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SalatRowCompact(icon: ImageVector, name: String, time: String, isActive: Boolean) {
    val textColor = if (isActive) Color(0xFF10B982) else Color.Black
    val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
    val bgColor = if (isActive) Color(0xFF10B982).copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 2.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = fontWeight,
                color = textColor,
                maxLines = 1
            )
        }
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
