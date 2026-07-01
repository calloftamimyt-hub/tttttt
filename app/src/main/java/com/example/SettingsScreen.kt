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
import androidx.compose.material.icons.filled.Settings
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

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

    // Logo management
    val logoPrefs = remember { context.getSharedPreferences("app_branding", Context.MODE_PRIVATE) }
    var selectedLogoUri by remember { mutableStateOf(logoPrefs.getString("app_logo_uri", null)?.let { Uri.parse(it) }) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Request persistent permission for the URI
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedLogoUri = it
                logoPrefs.edit().putString("app_logo_uri", it.toString()).apply()
            } catch (e: Exception) {
                // Fallback if persistable permission fails
                selectedLogoUri = it
                logoPrefs.edit().putString("app_logo_uri", it.toString()).apply()
            }
        }
    }

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

            // 2. Dark Mode Section
            Text(
                text = if (isEng) "Display Theme" else "ডিসপ্লে থিম",
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
                    val themePrefs = remember { context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE) }
                    var isDarkMode by remember { mutableStateOf(themePrefs.getBoolean("dark_mode", false)) }

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
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                            Column {
                                Text(
                                    text = if (isEng) "Dark Mode" else "ডার্ক মোড",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (isEng) "Enable dark theme for the app" else "অ্যাপের জন্য ডার্ক থিম চালু করুন",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = {
                                isDarkMode = it
                                themePrefs.edit().putBoolean("dark_mode", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen
                            )
                        )
                    }
                }
            }
        }
    }
}
