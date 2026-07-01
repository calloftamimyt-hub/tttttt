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
                    
                    // Vertical list of 5 Prayers (Clean & Premium Full Width Layout)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prayers.forEach { prayer ->
                            val isActive = prayer.internalName == state.currentPrayerName
                            val rowBg = if (isActive) Color(0xFFECFDF5) else Color.Transparent
                            val rowModifier = if (isActive) {
                                Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = primaryGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                            } else {
                                Modifier.fillMaxWidth()
                            }
                            
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = rowBg),
                                modifier = rowModifier
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Icon + Name + Countdown
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (isActive) primaryGreen else Color(0xFFF1F5F9),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = prayer.icon,
                                                contentDescription = null,
                                                tint = if (isActive) Color.White else Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        Column {
                                            Text(
                                                text = prayer.name,
                                                fontSize = 14.sp,
                                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (isActive) primaryGreen else Color(0xFF1E293B)
                                            )
                                            
                                            if (prayer.countdown.isNotEmpty()) {
                                                Text(
                                                    text = prayer.countdown,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isActive) primaryGreen else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Right: Time + Active Pill Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isActive) {
                                            Text(
                                                text = if (isEng) "Active" else "চলতি",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(primaryGreen, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        
                                        Text(
                                            text = formatTime(prayer.timeHours),
                                            fontSize = 14.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isActive) primaryGreen else Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
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


