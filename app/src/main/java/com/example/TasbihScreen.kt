package com.example

import android.content.Context
import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

data class DhikrItem(
    val englishName: String,
    val bengaliName: String,
    val arabic: String,
    val pronunciationEn: String,
    val pronunciationBn: String,
    val translationEn: String,
    val translationBn: String,
    val defaultTarget: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEng = GlobalLanguage.isEnglish
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // SharePreferences for local persistence / cache handling
    val sharedPrefs = remember { context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }

    // Dhikr List definition
    val dhikrList = remember {
        listOf(
            DhikrItem(
                englishName = "SubhanAllah",
                bengaliName = "সুবহানাল্লাহ",
                arabic = "سُبْحَانَ اللَّهِ",
                pronunciationEn = "SubhanAllah",
                pronunciationBn = "সুবহানাল্লাহ",
                translationEn = "Glory be to Allah",
                translationBn = "আল্লাহ পরম পবিত্র",
                defaultTarget = 33
            ),
            DhikrItem(
                englishName = "Alhamdulillah",
                bengaliName = "আলহামদুলিল্লাহ",
                arabic = "الْحَمْدُ لِلَّهِ",
                pronunciationEn = "Alhamdulillah",
                pronunciationBn = "আলহামদুলিল্লাহ",
                translationEn = "Praise be to Allah",
                translationBn = "সকল প্রশংসা আল্লাহর জন্য",
                defaultTarget = 33
            ),
            DhikrItem(
                englishName = "Allahu Akbar",
                bengaliName = "আল্লাহু আকবর",
                arabic = "اللَّهُ أَكْبَرُ",
                pronunciationEn = "Allahu Akbar",
                pronunciationBn = "আল্লাহু আকবর",
                translationEn = "Allah is the Greatest",
                translationBn = "আল্লাহ সর্বশ্রেষ্ঠ",
                defaultTarget = 34
            ),
            DhikrItem(
                englishName = "Astaghfirullah",
                bengaliName = "আস্তাগফিরুল্লাহ",
                arabic = "أَسْتَغْفِرُ اللَّهَ",
                pronunciationEn = "Astaghfirullah",
                pronunciationBn = "আস্তাগফিরুল্লাহ",
                translationEn = "I seek forgiveness from Allah",
                translationBn = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি",
                defaultTarget = 100
            ),
            DhikrItem(
                englishName = "La ilaha illallah",
                bengaliName = "লা ইলাহা ইল্লাল্লাহ",
                arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ",
                pronunciationEn = "La ilaha illallah",
                pronunciationBn = "লা ইলাহা ইল্লাল্লাহ",
                translationEn = "There is no god but Allah",
                translationBn = "আল্লাহ ছাড়া কোনো উপাস্য নেই",
                defaultTarget = 100
            ),
            DhikrItem(
                englishName = "Durood Shareef",
                bengaliName = "দরূদ শরীফ",
                arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ",
                pronunciationEn = "Allahumma salli ala Muhammad",
                pronunciationBn = "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ",
                translationEn = "O Allah, bestow peace upon Muhammad",
                translationBn = "হে আল্লাহ, মুহাম্মাদ (সাঃ) এর ওপর শান্তি বর্ষণ করুন",
                defaultTarget = 100
            ),
            DhikrItem(
                englishName = "Custom Dhikr",
                bengaliName = "কাস্টম জিকির",
                arabic = "ذِكْرٌ مُخَصَّصٌ",
                pronunciationEn = "Custom Dhikr",
                pronunciationBn = "কাস্টম জিকির",
                translationEn = "Set your own dhikr text and target count",
                translationBn = "নিজের মতো জিকির ও সংখ্যা সেট করুন",
                defaultTarget = 100
            )
        )
    }

    // State
    var selectedDhikrIndex by remember { mutableStateOf(sharedPrefs.getInt("selected_dhikr_idx", 0)) }
    val selectedDhikr = dhikrList.getOrElse(selectedDhikrIndex) { dhikrList.first() }

    // Read saved targets (especially for custom target)
    var targetCount by remember {
        mutableStateOf(sharedPrefs.getInt("target_count_${selectedDhikr.englishName}", selectedDhikr.defaultTarget))
    }

    var currentCount by remember { mutableStateOf(sharedPrefs.getInt("current_count_${selectedDhikr.englishName}", 0)) }
    var cycleCount by remember { mutableStateOf(sharedPrefs.getInt("cycle_count_${selectedDhikr.englishName}", 0)) }
    var lifetimeCount by remember { mutableStateOf(sharedPrefs.getInt("lifetime_count", 0)) }
    var todayCount by remember { mutableStateOf(sharedPrefs.getInt("today_count", 0)) }

    // Sound and haptic settings
    var soundEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("vibration_enabled", true)) }

    // Dialog flags
    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf(targetCount.toString()) }
    var showDhikrDropdown by remember { mutableStateOf(false) }

    // Helper to format numbers based on language
    fun formatNumber(num: Int): String {
        if (isEng) return num.toString()
        val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { if (it.isDigit()) banglaDigits[it - '0'] else it }.joinToString("")
    }

    // Sync state whenever selected Dhikr shifts
    LaunchedEffect(selectedDhikrIndex) {
        val activeDhikr = dhikrList[selectedDhikrIndex]
        targetCount = sharedPrefs.getInt("target_count_${activeDhikr.englishName}", activeDhikr.defaultTarget)
        currentCount = sharedPrefs.getInt("current_count_${activeDhikr.englishName}", 0)
        cycleCount = sharedPrefs.getInt("cycle_count_${activeDhikr.englishName}", 0)
    }

    // Function to increment counter
    val handleTap = {
        if (vibrationEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (soundEnabled) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }

        val newCount = currentCount + 1
        val updatedLifetime = lifetimeCount + 1
        val updatedToday = todayCount + 1

        lifetimeCount = updatedLifetime
        todayCount = updatedToday
        sharedPrefs.edit().putInt("lifetime_count", updatedLifetime).putInt("today_count", updatedToday).apply()

        if (newCount >= targetCount) {
            // Target achieved! Increment cycle and reset current
            val newCycle = cycleCount + 1
            cycleCount = newCycle
            currentCount = 0

            sharedPrefs.edit()
                .putInt("cycle_count_${selectedDhikr.englishName}", newCycle)
                .putInt("current_count_${selectedDhikr.englishName}", 0)
                .apply()

            // Distinctive celebration feedback
            if (vibrationEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Small delayed haptic feedback to signal cycle completion
                view.postDelayed({
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }, 150)
            }
        } else {
            currentCount = newCount
            sharedPrefs.edit().putInt("current_count_${selectedDhikr.englishName}", newCount).apply()
        }
    }

    // Function to reset counter
    val handleReset = {
        currentCount = 0
        cycleCount = 0
        sharedPrefs.edit()
            .putInt("current_count_${selectedDhikr.englishName}", 0)
            .putInt("cycle_count_${selectedDhikr.englishName}", 0)
            .apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEng) "Digital Tasbih" else "ডিজিটাল তাসবিহ",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset All",
                            tint = Color(0xFFEF4444)
                        )
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Selector Dropdown Row
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDhikrDropdown = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Album,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (isEng) selectedDhikr.englishName else selectedDhikr.bengaliName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = if (isEng) "Tap to change Dhikr" else "জিকির পরিবর্তন করতে ট্যাপ করুন",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = TextDark
                        )
                    }
                }

                DropdownMenu(
                    expanded = showDhikrDropdown,
                    onDismissRequest = { showDhikrDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    dhikrList.forEachIndexed { idx, item ->
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = if (isEng) item.englishName else item.bengaliName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (idx == selectedDhikrIndex) PrimaryGreen else TextDark
                                    )
                                    Text(
                                        text = item.arabic,
                                        fontSize = 13.sp,
                                        color = Color(0xFF10B981),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            onClick = {
                                selectedDhikrIndex = idx
                                sharedPrefs.edit().putInt("selected_dhikr_idx", idx).apply()
                                showDhikrDropdown = false
                            }
                        )
                        if (idx < dhikrList.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            // 2. Beautiful Islamic Arabic Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedDhikr.arabic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (isEng) selectedDhikr.pronunciationEn else selectedDhikr.pronunciationBn,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isEng) selectedDhikr.translationEn else selectedDhikr.translationBn,
                        fontSize = 11.5.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // 3. Main Circular Dial Counter with Tap Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .padding(12.dp)
            ) {
                // Background shadow dial decoration
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryGreen.copy(alpha = 0.04f), CircleShape)
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.12f), CircleShape)
                )

                // Circular Progress Arc
                val progress = if (targetCount > 0) currentCount.toFloat() / targetCount.toFloat() else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "Progress"
                )

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryGreen,
                    strokeWidth = 10.dp,
                    trackColor = Color(0xFFE2E8F0)
                )

                // Large Central Touch Button with Ripple & Shading
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFF8FAFC)),
                                radius = 300f
                            )
                        )
                        .border(1.5.dp, Color(0xFFE2E8F0), CircleShape)
                        .clickable { handleTap() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Current Count Text
                        Text(
                            text = formatNumber(currentCount),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Target Text (Editable on click)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    customTargetInput = targetCount.toString()
                                    showCustomTargetDialog = true
                                }
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${if (isEng) "Target" else "লক্ষ্য"}: ${formatNumber(targetCount)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Target",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cycle/Lap indicator
                        Text(
                            text = "${if (isEng) "Cycle" else "চক্র"}: ${formatNumber(cycleCount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                    }
                }
            }

            // 4. Quick Toggles and Reset Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Toggle
                IconButton(
                    onClick = {
                        soundEnabled = !soundEnabled
                        sharedPrefs.edit().putBoolean("sound_enabled", soundEnabled).apply()
                    },
                    modifier = Modifier
                        .background(
                            if (soundEnabled) PrimaryGreen.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = if (soundEnabled) PrimaryGreen else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Haptic Toggle
                IconButton(
                    onClick = {
                        vibrationEnabled = !vibrationEnabled
                        sharedPrefs.edit().putBoolean("vibration_enabled", vibrationEnabled).apply()
                    },
                    modifier = Modifier
                        .background(
                            if (vibrationEnabled) PrimaryGreen.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.Circle,
                        contentDescription = "Vibration Toggle",
                        tint = if (vibrationEnabled) PrimaryGreen else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Counter Reset Button (For current Dhikr only)
                IconButton(
                    onClick = { handleReset() },
                    modifier = Modifier
                        .background(Color(0xFFFFF1F2), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Current",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 5. Statistics Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEng) "Today's Total" else "আজকের জিকির",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatNumber(todayCount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEng) "Lifetime Total" else "সর্বমোট জিকির",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatNumber(lifetimeCount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }
    }

    // Reset All Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = if (isEng) "Reset All Statistics?" else "সকল পরিসংখ্যান রিসেট করবেন?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = if (isEng) "This will completely clear your daily and lifetime dhikr statistics. This cannot be undone."
                           else "এটি আপনার আজকের এবং সর্বমোট জিকিরের সমস্ত পরিসংখ্যান সম্পূর্ণরূপে মুছে ফেলবে। এটি আর ফিরিয়ে আনা সম্ভব নয়।",
                    fontSize = 13.sp,
                    color = TextGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sharedPrefs.edit()
                            .putInt("lifetime_count", 0)
                            .putInt("today_count", 0)
                            .apply()
                        lifetimeCount = 0
                        todayCount = 0
                        handleReset()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isEng) "Reset" else "রিসেট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isEng) "Cancel" else "বাতিল")
                }
            }
        )
    }

    // Custom Target Setter Dialog
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = {
                Text(
                    text = if (isEng) "Set Target Count" else "টার্গেট সংখ্যা সেট করুন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEng) "Enter a custom goal for this Dhikr session:" 
                               else "এই জিকিরের জন্য আপনার কাঙ্ক্ষিত টার্গেট সংখ্যা লিখুন:",
                        fontSize = 12.5.sp,
                        color = TextGray
                    )
                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { customTargetInput = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customTargetInput.toIntOrNull() ?: selectedDhikr.defaultTarget
                        if (parsed in 1..99999) {
                            targetCount = parsed
                            sharedPrefs.edit().putInt("target_count_${selectedDhikr.englishName}", parsed).apply()
                        }
                        showCustomTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(if (isEng) "Save" else "সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text(if (isEng) "Cancel" else "বাতিল")
                }
            }
        )
    }
}
