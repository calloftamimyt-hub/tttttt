package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.AppLanguages
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel? = null,
    prayerAlarms: Map<String, Boolean> = emptyMap(),
    onTogglePrayerAlarm: (String) -> Unit = {},
    isAutoLocation: Boolean = true,
    onToggleAutoLocation: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val isEng = GlobalLanguage.isEnglish
    val scrollState = rememberScrollState()

    // Retrieve settings state from shared preferences or viewModel
    val selectedCountryCode = viewModel?.selectedCountryCode?.collectAsState()?.value ?: "BD"
    val selectedLanguage = viewModel?.language?.collectAsState()?.value ?: AppLanguages.BENGALI

    // General options
    val sharedPrefs = remember { context.getSharedPreferences("app_settings_general", Context.MODE_PRIVATE) }
    var soundEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("vibration_enabled", true)) }
    var trackerReminderEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("tracker_reminder", true)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEng) "App Settings" else "অ্যাপ সেটিংস",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 1. Language and Region Section
            Text(
                text = if (isEng) "Language & Region" else "ভাষা ও অঞ্চল",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Language option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryGreen)
                            Column {
                                Text(
                                    text = if (isEng) "App Language" else "অ্যাপের ভাষা",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (selectedLanguage == AppLanguages.ENGLISH) "English" else "বাংলা",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }

                        // Toggle Language Button Group
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            val activeColor = Color.White
                            val inactiveColor = Color.Transparent
                            val activeTextColor = PrimaryGreen
                            val inactiveTextColor = Color(0xFF475569)

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedLanguage == AppLanguages.BENGALI) activeColor else inactiveColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        viewModel?.setSelectedCountryAndLanguage(selectedCountryCode, AppLanguages.BENGALI)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "বাংলা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == AppLanguages.BENGALI) activeTextColor else inactiveTextColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedLanguage == AppLanguages.ENGLISH) activeColor else inactiveColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        viewModel?.setSelectedCountryAndLanguage(selectedCountryCode, AppLanguages.ENGLISH)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "English",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == AppLanguages.ENGLISH) activeTextColor else inactiveTextColor
                                )
                            }
                        }
                    }
                }
            }

            // Location Settings Section
            Text(
                text = if (isEng) "Location Settings" else "অবস্থান সেটিংস",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                            Column {
                                Text(
                                    text = if (isEng) "Automatic Location" else "স্বয়ংক্রিয় অবস্থান",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (isEng) 
                                        "Update prayer times based on current coordinates" 
                                    else 
                                        "বর্তমান স্থানাঙ্কের ভিত্তিতে নামাজের সময় আপডেট করুন",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Switch(
                            checked = isAutoLocation,
                            onCheckedChange = onToggleAutoLocation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen
                            )
                        )
                    }
                }
            }

            // 2. Prayer Alarms Section
            Text(
                text = if (isEng) "Salat Prayer Alarms" else "নামাজের ওয়াক্ত অ্যালার্ম",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val prayers = listOf(
                        "Fajr" to (if (isEng) "Fajr Alarm" else "ফজর অ্যালার্ম"),
                        "Dhuhr" to (if (isEng) "Dhuhr Alarm" else "যোহর অ্যালার্ম"),
                        "Asr" to (if (isEng) "Asr Alarm" else "আসর অ্যালার্ম"),
                        "Maghrib" to (if (isEng) "Maghrib Alarm" else "মাগরিব অ্যালার্ম"),
                        "Isha" to (if (isEng) "Isha Alarm" else "এশা অ্যালার্ম")
                    )

                    prayers.forEachIndexed { index, (key, label) ->
                        val isAlarmOn = prayerAlarms[key] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = if (isAlarmOn) PrimaryGreen else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                            Switch(
                                checked = isAlarmOn,
                                onCheckedChange = { onTogglePrayerAlarm(key) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryGreen
                                )
                            )
                        }
                        if (index < prayers.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        }
                    }
                }
            }

            // 3. Audio & Notifications Section
            Text(
                text = if (isEng) "Sound & Notifications" else "শব্দ ও নোটিফিকেশন",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Sound Enabled
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF3B82F6))
                            Column {
                                Text(
                                    text = if (isEng) "Adhan Sound" else "আজানের শব্দ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = if (isEng) "Play full adhan sound on time" else "নির্ধারিত সময়ে আজানের ফুল শব্দ বাজবে",
                                    fontSize = 10.5.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                sharedPrefs.edit().putBoolean("sound_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Vibration Enabled
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF8B5CF6))
                            Column {
                                Text(
                                    text = if (isEng) "Vibration Alert" else "ভাইব্রেশন অ্যালার্ট",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = if (isEng) "Vibrate on silent mode alarms" else "সাইলেন্ট মোডে ডিভাইস ভাইব্রেট করবে",
                                    fontSize = 10.5.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                sharedPrefs.edit().putBoolean("vibration_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8B5CF6)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Daily Tracker Reminder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFFEC4899))
                            Column {
                                Text(
                                    text = if (isEng) "Daily Activity Reminders" else "দৈনিক আমল রিমাইন্ডার",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = if (isEng) "Notify to update tracking stats" else "আমল ও নামাজের ট্র্যাকার হালনাগাদের নোটিফিকেশন",
                                    fontSize = 10.5.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Switch(
                            checked = trackerReminderEnabled,
                            onCheckedChange = {
                                trackerReminderEnabled = it
                                sharedPrefs.edit().putBoolean("tracker_reminder", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEC4899)
                            )
                        )
                    }
                }
            }
        }
    }
}
