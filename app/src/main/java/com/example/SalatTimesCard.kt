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
        val primaryGreen = Color(0xFF388E3C) // Use a slightly darker green closer to the image
        val lightGreen = Color(0xFFE8F5E9)

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
        
        val formatTimeJustAMPM = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            if (hour >= 12) "PM" else "AM"
        }
        
        val formatTimeWithoutAMPM = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d", displayHour, min)
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

        val currentIndex = prayers.indexOfFirst { it.internalName == state.currentPrayerName }.takeIf { it >= 0 } ?: 0
        val currentPrayer = prayers[currentIndex]
        val nextIndex = (currentIndex + 1) % 5
        val nextPrayer = prayers[nextIndex]
        
        // Remove current prayer from the sides list
        val sidePrayers = prayers.filterIndexed { index, _ -> index != currentIndex }
        
        val leftPrayers = sidePrayers.take(2)
        val rightPrayers = sidePrayers.takeLast(2)

        val dateFormatter = SimpleDateFormat("dd MMMM, yyyy", Locale("bn"))
        val dateStr = "আজ " + dateFormatter.format(Date()).toBengali()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FARZ PRAYERS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Mosque,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEng) "Farz Prayers" else "ফরজ নামাজের সময়",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(lightGreen, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isEng) "Today" else dateStr,
                                color = primaryGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Sunrise / Sunset Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEng) "Sunrise" else "সূর্যোদয়",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = formatTime(times.sunriseHours),
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.WbTwilight, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEng) "Sunset" else "সূর্যাস্ত",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = formatTime(times.maghribHours),
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // The Big Prayers Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left column
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            leftPrayers.forEach { prayer ->
                                SidePrayerItem(
                                    name = prayer.name,
                                    timeWithoutAMPM = formatTimeWithoutAMPM(prayer.timeHours),
                                    ampm = formatTimeJustAMPM(prayer.timeHours),
                                    icon = prayer.icon,
                                    primaryGreen = primaryGreen,
                                    isEng = isEng
                                )
                            }
                        }
                        
                        // Center Circle
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .border(4.dp, primaryGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(primaryGreen, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEng) "Now" else "এখন",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = currentPrayer.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                
                                Text(
                                    text = currentPrayer.countdown,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryGreen
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = (if (isEng) "Next: " else "পরবর্তী: ") + nextPrayer.name + " " + formatTime(nextPrayer.timeHours),
                                    fontSize = 9.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Right column
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rightPrayers.forEach { prayer ->
                                SidePrayerItem(
                                    name = prayer.name,
                                    timeWithoutAMPM = formatTimeWithoutAMPM(prayer.timeHours),
                                    ampm = formatTimeJustAMPM(prayer.timeHours),
                                    icon = prayer.icon,
                                    primaryGreen = primaryGreen,
                                    isEng = isEng
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // NAFAL PRAYERS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mosque,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEng) "Nafil Prayers" else "নফল সালাতের সময়",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val nafilPrayers = listOf(
                        Triple(if (isEng) "Tahajjud" else "তাহাজ্জুদ", formatTime(times.fajrHours - 1.2), Icons.Outlined.Mosque),
                        Triple(if (isEng) "Ishraq" else "ইশরাক", formatTime(times.sunriseHours + 0.3), Icons.Outlined.Mosque),
                        Triple(if (isEng) "Chasht" else "চাশত", formatTime(times.sunriseHours + 1.5), Icons.Outlined.Mosque),
                        Triple(if (isEng) "Duha" else "দুহা", formatTime(times.sunriseHours + 2.5), Icons.Outlined.ModeNight),
                        Triple(if (isEng) "Awwabin" else "আওয়াবিন", formatTime(times.maghribHours + 0.3), Icons.Outlined.Mosque),
                        Triple(if (isEng) "Tahiyatul Masjid" else "তাহিইয়াতুল মসজিদ", "-", Icons.Outlined.WbTwilight)
                    )
                    
                    nafilPrayers.forEachIndexed { index, prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = prayer.third,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = prayer.first,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (prayer.second != "-") {
                                    Box(
                                        modifier = Modifier
                                            .background(lightGreen, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = prayer.second,
                                            color = primaryGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "-",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (index < nafilPrayers.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SidePrayerItem(
    name: String,
    timeWithoutAMPM: String,
    ampm: String,
    icon: ImageVector,
    primaryGreen: Color,
    isEng: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = "•",
            fontSize = 10.sp,
            color = primaryGreen,
            modifier = Modifier.padding(vertical = 1.dp)
        )
        Text(
            text = timeWithoutAMPM,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = if (isEng) ampm else ampm.toBengali(),
            fontSize = 10.sp,
            color = Color(0xFF475569)
        )
    }
}

data class PrayerItem(
    val name: String,
    val timeHours: Double,
    val icon: ImageVector,
    val countdown: String,
    val internalName: String
)

