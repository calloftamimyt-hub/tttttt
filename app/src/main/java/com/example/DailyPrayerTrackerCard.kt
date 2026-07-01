package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.toBengali
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyPrayerTrackerCard(
    onNavigateToTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish
    val primaryGreen = Color(0xFF10B982)

    val trackerPrefs = remember { context.getSharedPreferences("daily_tracker_prefs", Context.MODE_PRIVATE) }
    val dateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val checkedState = remember { mutableStateMapOf<String, Boolean>() }

    // Load initial states
    LaunchedEffect(dateStr) {
        listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { key ->
            checkedState[key] = trackerPrefs.getBoolean("${dateStr}_$key", false)
        }
    }

    // Live update when modified anywhere else (e.g. on full TrackerScreen or SalatTimesCard)
    DisposableEffect(trackerPrefs, dateStr) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != null && key.startsWith(dateStr)) {
                val prayerKey = key.substringAfter("${dateStr}_")
                if (prayerKey in listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")) {
                    checkedState[prayerKey] = trackerPrefs.getBoolean(key, false)
                }
            }
        }
        trackerPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            trackerPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val completedCount = checkedState.values.count { it }

    val prayersList = listOf(
        Triple("Fajr", if (isEng) "Fajr" else "ফজর", Icons.Outlined.WbTwilight),
        Triple("Dhuhr", if (isEng) "Dhuhr" else "যোহর", Icons.Outlined.WbSunny),
        Triple("Asr", if (isEng) "Asr" else "আসর", Icons.Outlined.WbSunny),
        Triple("Maghrib", if (isEng) "Maghrib" else "মাগরিব", Icons.Outlined.Cloud),
        Triple("Isha", if (isEng) "Isha" else "এশা", Icons.Outlined.ModeNight)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header: Title and "Full Tracker" link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isEng) "Daily Prayer Tracker" else "আজকের নামাজ ট্র্যাকার",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                }

                TextButton(
                    onClick = onNavigateToTracker,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isEng) "Full Tracker" else "সম্পূর্ণ ট্র্যাকার",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryGreen
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress status and bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val countText = if (isEng) "$completedCount of 5 Completed" else "$completedCount/৫ সম্পন্ন হয়েছে"
                Text(
                    text = countText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen
                )

                val percentage = (completedCount * 100) / 5
                val percentText = if (isEng) "$percentage%" else "${percentage.toString().toBengali()}%"
                Text(
                    text = percentText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth dynamic progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
            ) {
                val progressFloat = completedCount / 5f
                if (progressFloat > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFloat)
                            .fillMaxHeight()
                            .background(primaryGreen, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5 horizontal custom check-off buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                prayersList.forEach { (internalName, displayName, icon) ->
                    val isChecked = checkedState[internalName] ?: false

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newValue = !isChecked
                                checkedState[internalName] = newValue
                                trackerPrefs.edit().putBoolean("${dateStr}_$internalName", newValue).apply()

                                val toastMsg = if (isEng) {
                                    if (newValue) "$internalName Checked!" else "$internalName Unchecked!"
                                } else {
                                    val benName = when(internalName) {
                                        "Fajr" -> "ফজর"
                                        "Dhuhr" -> "যোহর"
                                        "Asr" -> "আসর"
                                        "Maghrib" -> "মাগরিব"
                                        else -> "এশা"
                                    }
                                    if (newValue) "$benName আদায় করা হয়েছে!" else "$benName আনমার্ক করা হয়েছে!"
                                }
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp) // Touch target minimum
                                .background(
                                    color = if (isChecked) primaryGreen.copy(alpha = 0.15f) else (if (isDarkModeGlobal) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isChecked) primaryGreen else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completed",
                                    tint = primaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = displayName,
                                    tint = TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isChecked) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isChecked) primaryGreen else TextDark
                        )
                    }
                }
            }
        }
    }
}
