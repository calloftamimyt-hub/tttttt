package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.ViewState
import com.example.viewmodel.toBengali
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalatTimesCard(state: ViewState) {
    state.prayerTimes?.let { times ->
        val isEng = GlobalLanguage.isEnglish
        val primaryGreen = Color(0xFF10B982)
        val lightGreen = Color(0xFFECFDF5)

        // Formatting Helpers
        val formatTime = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val p = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d %s", displayHour, min, p)
            if (isEng) timeStr else timeStr.toBengali()
        }

        // Current prayer logic
        val prayers = listOf(
            PrayerItem(if (isEng) "Fajr" else "ফজর", times.fajrHours, Icons.Outlined.Mosque, state.fajrCountdown, "Fajr"),
            PrayerItem(if (isEng) "Dhuhr" else "যোহর", times.dhuhrHours, Icons.Outlined.WbSunny, state.dhuhrCountdown, "Dhuhr"),
            PrayerItem(if (isEng) "Asr" else "আসর", times.asrHours, Icons.Outlined.Cloud, state.asrCountdown, "Asr"),
            PrayerItem(if (isEng) "Maghrib" else "মাগরিব", times.maghribHours, Icons.Outlined.Mosque, state.maghribCountdown, "Maghrib"),
            PrayerItem(if (isEng) "Isha" else "এশা", times.ishaHours, Icons.Outlined.ModeNight, state.ishaCountdown, "Isha")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp), // Minimal gap between cards for FB Feed style
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // FARZ PRAYERS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEng) "Farz Prayers" else "ফরজ নামাজ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        
                        Text(
                            text = if (isEng) "Today" else "আজ",
                            color = primaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(lightGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 16:9 Compact Layout Content
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: 5 Prayers (Slim Layout)
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp) // tighter spacing
                        ) {
                            prayers.forEach { prayer ->
                                val isActive = prayer.internalName == state.currentPrayerName
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prayer.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isActive) primaryGreen else Color(0xFF475569)
                                    )
                                    Text(
                                        text = formatTime(prayer.timeHours),
                                        fontSize = 14.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isActive) primaryGreen else Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                        
                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(120.dp)
                                .background(Color(0xFFF1F5F9))
                        )
                        
                        // Right: Iftar & Countdown
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isEng) "Iftar" else "ইফতার",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Circle + Icon alignment
                            Box(
                                modifier = Modifier.offset(y = (-4).dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Countdown Circle
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, primaryGreen, CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = state.maghribCountdown.ifEmpty { "00:00:00" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryGreen
                                            )
                                            Text(
                                                text = if (isEng) "remaining" else "বাকি",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.offset(y = (-2).dp)
                                            )
                                        }
                                    }
                                    
                                    // Sunset Icon positioned on the right middle
                                    Icon(
                                        imageVector = Icons.Outlined.WbTwilight,
                                        contentDescription = "Sunset",
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = formatTime(times.maghribHours),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
            
            // NAFAL PRAYERS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header
                    Text(
                        text = if (isEng) "Nafil Prayers" else "নফল সালাত",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val nafilPrayers = listOf(
                        Triple(if (isEng) "Tahajjud" else "তাহাজ্জুদ", formatTime(times.fajrHours - 1.2), Icons.Outlined.ModeNight),
                        Triple(if (isEng) "Ishraq" else "ইশরাক", formatTime(times.sunriseHours + 0.3), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Chasht" else "চাশত", formatTime(times.sunriseHours + 1.5), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Duha" else "দুহা", formatTime(times.sunriseHours + 2.5), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Awwabin" else "আওয়াবিন", formatTime(times.maghribHours + 0.3), Icons.Outlined.WbTwilight)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        nafilPrayers.forEach { prayer ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = prayer.third,
                                        contentDescription = null,
                                        tint = primaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prayer.first,
                                        fontSize = 14.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Text(
                                    text = prayer.second,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PrayerItem(
    val name: String,
    val timeHours: Double,
    val icon: ImageVector,
    val countdown: String,
    val internalName: String
)


