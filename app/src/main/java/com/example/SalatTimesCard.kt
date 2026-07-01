package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
        val context = LocalContext.current
        val isEng = GlobalLanguage.isEnglish
        val primaryGreen = Color(0xFF10B982)
        val lightGreen = Color(0xFFECFDF5)

        val trackerPrefs = remember { context.getSharedPreferences("daily_tracker_prefs", Context.MODE_PRIVATE) }
        val dateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
        val checkedState = remember { mutableStateMapOf<String, Boolean>() }

        LaunchedEffect(dateStr) {
            listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { key ->
                checkedState[key] = trackerPrefs.getBoolean("${dateStr}_$key", false)
            }
        }

        // Formatting Helpers
        val formatTimeNoAmPm = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d", displayHour, min)
            if (isEng) timeStr else timeStr.toBengali()
        }

        val formatTimeWithAmPm = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val p = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d %s", displayHour, min, p)
            if (isEng) timeStr else timeStr.toBengali()
        }

        // 5 Prayers items with respective range bounds
        val prayers = listOf(
            PrayerRangeItem(
                name = if (isEng) "Fajr" else "ফজর",
                startTimeHours = times.fajrHours,
                endTimeHours = times.sunriseHours,
                icon = Icons.Outlined.WbTwilight,
                internalName = "Fajr"
            ),
            PrayerRangeItem(
                name = if (isEng) "Dhuhr" else "যুহর",
                startTimeHours = times.dhuhrHours,
                endTimeHours = times.asrHours,
                icon = Icons.Outlined.WbSunny,
                internalName = "Dhuhr"
            ),
            PrayerRangeItem(
                name = if (isEng) "Asr" else "আসর",
                startTimeHours = times.asrHours,
                endTimeHours = times.maghribHours,
                icon = Icons.Outlined.WbSunny,
                internalName = "Asr"
            ),
            PrayerRangeItem(
                name = if (isEng) "Maghrib" else "মাগরিব",
                startTimeHours = times.maghribHours,
                endTimeHours = times.ishaHours,
                icon = Icons.Outlined.Cloud,
                internalName = "Maghrib"
            ),
            PrayerRangeItem(
                name = if (isEng) "Isha" else "ইশা",
                startTimeHours = times.ishaHours,
                endTimeHours = times.fajrHours, // Approximation to simplify range bound
                icon = Icons.Outlined.ModeNight,
                internalName = "Isha"
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
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
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Vertical list of 5 Prayers (Clean, slim & beautifully compact layout)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        prayers.forEach { prayer ->
                            val isActive = prayer.internalName == state.currentPrayerName
                            val isChecked = checkedState[prayer.internalName] ?: false
                            
                            val currentActive = state.currentPrayerName
                            val currentHour = state.currentHourDecimal
                            val progress = when (prayer.internalName) {
                                "Fajr" -> {
                                    if (currentActive == "Fajr") {
                                        val diff = prayer.endTimeHours - prayer.startTimeHours
                                        if (diff > 0) ((currentHour - prayer.startTimeHours) / diff).coerceIn(0.0, 1.0).toFloat() else 0f
                                    } else if (currentActive in listOf("Duha", "Dhuhr", "Asr", "Maghrib", "Isha")) {
                                        1f
                                    } else {
                                        0f
                                    }
                                }
                                "Dhuhr" -> {
                                    if (currentActive == "Dhuhr") {
                                        val diff = prayer.endTimeHours - prayer.startTimeHours
                                        if (diff > 0) ((currentHour - prayer.startTimeHours) / diff).coerceIn(0.0, 1.0).toFloat() else 0f
                                    } else if (currentActive in listOf("Asr", "Maghrib", "Isha")) {
                                        1f
                                    } else {
                                        0f
                                    }
                                }
                                "Asr" -> {
                                    if (currentActive == "Asr") {
                                        val diff = prayer.endTimeHours - prayer.startTimeHours
                                        if (diff > 0) ((currentHour - prayer.startTimeHours) / diff).coerceIn(0.0, 1.0).toFloat() else 0f
                                    } else if (currentActive in listOf("Maghrib", "Isha")) {
                                        1f
                                    } else {
                                        0f
                                    }
                                }
                                "Maghrib" -> {
                                    if (currentActive == "Maghrib") {
                                        val diff = prayer.endTimeHours - prayer.startTimeHours
                                        if (diff > 0) ((currentHour - prayer.startTimeHours) / diff).coerceIn(0.0, 1.0).toFloat() else 0f
                                    } else if (currentActive == "Isha") {
                                        1f
                                    } else {
                                        0f
                                    }
                                }
                                "Isha" -> {
                                    if (currentActive == "Isha") {
                                        val shiftedEnd = prayer.endTimeHours + 24.0
                                        val shiftedCurrent = if (currentHour < prayer.startTimeHours) currentHour + 24.0 else currentHour
                                        val diff = shiftedEnd - prayer.startTimeHours
                                        if (diff > 0) ((shiftedCurrent - prayer.startTimeHours) / diff).coerceIn(0.0, 1.0).toFloat() else 0f
                                    } else {
                                        0f
                                    }
                                }
                                else -> 0f
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newValue = !isChecked
                                        checkedState[prayer.internalName] = newValue
                                        trackerPrefs.edit().putBoolean("${dateStr}_${prayer.internalName}", newValue).apply()
                                        
                                        val toastMsg = if (isEng) {
                                            if (newValue) "${prayer.internalName} Marked as Prayed!" else "${prayer.internalName} Unmarked!"
                                        } else {
                                            val benName = when(prayer.internalName) {
                                                "Fajr" -> "ফজর"
                                                "Dhuhr" -> "যুহর"
                                                "Asr" -> "আসর"
                                                "Maghrib" -> "মাগরিব"
                                                else -> "ইশা"
                                            }
                                            if (newValue) "$benName আদায় করা হয়েছে!" else "$benName আনমার্ক করা হয়েছে!"
                                        }
                                        android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .background(if (isActive) lightGreen.copy(alpha = 0.5f) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Side: Icon + Prayer Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = prayer.icon,
                                            contentDescription = null,
                                            tint = if (isActive) primaryGreen else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        
                                        Text(
                                            text = prayer.name,
                                            fontSize = 14.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isActive) primaryGreen else Color(0xFF1E293B)
                                        )
                                    }
                                    
                                    // Middle Side: Start Time - End Time range (e.g., ০৩:৪৭ - ০৫:১৩)
                                    Text(
                                        text = "${formatTimeNoAmPm(prayer.startTimeHours)} - ${formatTimeNoAmPm(prayer.endTimeHours)}",
                                        fontSize = 14.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isActive) primaryGreen else Color(0xFF334155),
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    
                                    // Right Side: Simple Radio/Checkbox Circle
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                            contentDescription = null,
                                            tint = if (isChecked) primaryGreen else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                // Beautiful, premium visual progress bar
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    val trackColor = when {
                                        isActive -> primaryGreen.copy(alpha = 0.15f)
                                        progress == 1f -> Color(0xFFE2E8F0)
                                        else -> Color(0xFFF1F5F9)
                                    }
                                    val barHeight = if (isActive) 5.dp else 2.dp
                                    val progressColor = if (isActive) primaryGreen else if (progress == 1f) Color(0xFFCBD5E1) else Color.Transparent
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeight)
                                            .background(trackColor, RoundedCornerShape(2.5.dp))
                                    ) {
                                        if (progress > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress)
                                                    .height(barHeight)
                                                    .background(progressColor, RoundedCornerShape(2.5.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Aligned bottom Makruh period with orange dot
                    val makruhStart = times.dhuhrHours - 15.0 / 60.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFEA580C), CircleShape) // Orange dot
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEng) "Makruh: ${formatTimeNoAmPm(makruhStart)}" else "মাকরুহ: ${formatTimeNoAmPm(makruhStart)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
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
                        Triple(if (isEng) "Tahajjud" else "তাহাজ্জুদ", formatTimeWithAmPm(times.fajrHours - 1.2), Icons.Outlined.ModeNight),
                        Triple(if (isEng) "Ishraq" else "ইশরাক", formatTimeWithAmPm(times.sunriseHours + 0.3), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Chasht" else "চাশত", formatTimeWithAmPm(times.sunriseHours + 1.5), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Duha" else "দুহা", formatTimeWithAmPm(times.sunriseHours + 2.5), Icons.Outlined.WbSunny),
                        Triple(if (isEng) "Awwabin" else "আওয়াবিন", formatTimeWithAmPm(times.maghribHours + 0.3), Icons.Outlined.WbTwilight)
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

data class PrayerRangeItem(
    val name: String,
    val startTimeHours: Double,
    val endTimeHours: Double,
    val icon: ImageVector,
    val internalName: String
)



