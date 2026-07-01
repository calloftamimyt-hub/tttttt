package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.BgLight
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.toBengali
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val isEnglish = GlobalLanguage.isEnglish
    
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayDateFormatter = remember(isEnglish) { 
        SimpleDateFormat(if (isEnglish) "EEE, dd MMM yyyy" else "EEE, dd MMM yyyy", if (isEnglish) Locale.ENGLISH else Locale("bn", "BD")) 
    }

    // State for the tracker
    var fajr by remember { mutableStateOf(false) }
    var dhuhr by remember { mutableStateOf(false) }
    var asr by remember { mutableStateOf(false) }
    var maghrib by remember { mutableStateOf(false) }
    var isha by remember { mutableStateOf(false) }
    var quran by remember { mutableStateOf(false) }
    var charity by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf(false) }
    var istighfar by remember { mutableStateOf(false) }
    var parents by remember { mutableStateOf(false) }
    var tasbihCount by remember { mutableIntStateOf(0) }

    val trackerPrefs = remember { context.getSharedPreferences("daily_tracker_prefs", android.content.Context.MODE_PRIVATE) }
    val dateKey = remember(selectedDate) { dateFormatter.format(selectedDate.time) }

    LaunchedEffect(dateKey) {
        fajr = trackerPrefs.getBoolean("${dateKey}_Fajr", false)
        dhuhr = trackerPrefs.getBoolean("${dateKey}_Dhuhr", false)
        asr = trackerPrefs.getBoolean("${dateKey}_Asr", false)
        maghrib = trackerPrefs.getBoolean("${dateKey}_Maghrib", false)
        isha = trackerPrefs.getBoolean("${dateKey}_Isha", false)
        quran = trackerPrefs.getBoolean("${dateKey}_quran", false)
        charity = trackerPrefs.getBoolean("${dateKey}_charity", false)
        reading = trackerPrefs.getBoolean("${dateKey}_reading", false)
        istighfar = trackerPrefs.getBoolean("${dateKey}_istighfar", false)
        parents = trackerPrefs.getBoolean("${dateKey}_parents", false)
        tasbihCount = trackerPrefs.getInt("${dateKey}_tasbihCount", 0)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                    Text(
                        text = if (isEnglish) "Daily Activity Tracker" else "দৈনিক আমল ট্র্যাকার",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // To center the title
                }
                
                // Date Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        selectedDate = (selectedDate.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = PrimaryGreen)
                    }
                    
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { /* Show DatePicker if needed */ }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = PrimaryGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = displayDateFormatter.format(selectedDate.time),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = {
                        val today = Calendar.getInstance()
                        if (selectedDate.before(today)) {
                            selectedDate = (selectedDate.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = if (selectedDate.before(Calendar.getInstance())) PrimaryGreen else Color.LightGray)
                    }
                }
            }
        },
        containerColor = BgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prayers Section
            TrackerSection(title = if (isEnglish) "Five Daily Prayers" else "পাঁচ ওয়াক্ত নামাজ") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrayerItem(if (isEnglish) "Fajr" else "ফজর", fajr) { fajr = it }
                    PrayerItem(if (isEnglish) "Dhuhr" else "যোহর", dhuhr) { dhuhr = it }
                    PrayerItem(if (isEnglish) "Asr" else "আসর", asr) { asr = it }
                    PrayerItem(if (isEnglish) "Maghrib" else "মাগরিব", maghrib) { maghrib = it }
                    PrayerItem(if (isEnglish) "Isha" else "এশা", isha) { isha = it }
                }
            }

            // Other Activities
            TrackerSection(title = if (isEnglish) "Spiritual & Good Deeds" else "অন্যান্য আমল ও নেক কাজ") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityToggleItem(if (isEnglish) "Quran Recitation" else "কুরআন তিলাওয়াত", Icons.Outlined.MenuBook, quran) { quran = it }
                    ActivityToggleItem(if (isEnglish) "Daily Sadaqah" else "দৈনিক দান/সদাকাহ", Icons.Outlined.VolunteerActivism, charity) { charity = it }
                    ActivityToggleItem(if (isEnglish) "Islamic Reading" else "ইসলামিক জ্ঞান অর্জন", Icons.Outlined.ImportContacts, reading) { reading = it }
                    ActivityToggleItem(if (isEnglish) "Istighfar (100+)" else "ইস্তিগফার (১০০+)", Icons.Outlined.SettingsBackupRestore, istighfar) { istighfar = it }
                    ActivityToggleItem(if (isEnglish) "Serving Parents" else "পিতামাতার সেবা", Icons.Outlined.Favorite, parents) { parents = it }
                }
            }

            // Tasbih Section
            TrackerSection(title = if (isEnglish) "Tasbih Counter" else "তসবীহ কাউন্টার") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isEnglish) "Total Tasbih Count" else "মোট তসবীহ সংখ্যা",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = if (isEnglish) tasbihCount.toString() else tasbihCount.toString().toBengali(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (tasbihCount > 0) tasbihCount-- },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextDark)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Button(
                            onClick = { tasbihCount++ },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    trackerPrefs.edit()
                        .putBoolean("${dateKey}_Fajr", fajr)
                        .putBoolean("${dateKey}_Dhuhr", dhuhr)
                        .putBoolean("${dateKey}_Asr", asr)
                        .putBoolean("${dateKey}_Maghrib", maghrib)
                        .putBoolean("${dateKey}_Isha", isha)
                        .putBoolean("${dateKey}_quran", quran)
                        .putBoolean("${dateKey}_charity", charity)
                        .putBoolean("${dateKey}_reading", reading)
                        .putBoolean("${dateKey}_istighfar", istighfar)
                        .putBoolean("${dateKey}_parents", parents)
                        .putInt("${dateKey}_tasbihCount", tasbihCount)
                        .apply()
                    android.widget.Toast.makeText(context, if (isEnglish) "Tracker Saved Successfully!" else "আমল ট্র্যাকার সেভ করা হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) "Save Today's Progress" else "আজকের আমল সেভ করুন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TrackerSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun PrayerItem(name: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isChecked) PrimaryGreen.copy(alpha = 0.1f) else Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (isChecked) PrimaryGreen else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isChecked) PrimaryGreen else TextDark,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
        )
    }
}

@Composable
fun ActivityToggleItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isChecked) Color(0xFFE8F5E9) else Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) PrimaryGreen else TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 15.sp,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
        )
    }
}
