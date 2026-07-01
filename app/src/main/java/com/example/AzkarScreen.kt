package com.example

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

data class AzkarItem(
    val id: String,
    val arabic: String,
    val pronEng: String,
    val pronBn: String,
    val transEng: String,
    val transBn: String,
    val virtueEng: String,
    val virtueBn: String,
    val targetCount: Int,
    val type: String // "morning", "evening"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish
    val prefs = remember { context.getSharedPreferences("azkar_counts_prefs", Context.MODE_PRIVATE) }

    val azkarList = remember {
        listOf(
            AzkarItem(
                id = "azkar_ayat_kursi",
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ...",
                pronEng = "Allahu la ilaha illa Huwal-Hayyul-Qayyum...",
                pronBn = "আল্লাহু লা ইলাহা ইল্লা হুয়াল হাইয়্যুল কাইয়্যুম...",
                transEng = "Allah! There is no deity except Him, the Ever Living, the Sustainer of all existence...",
                transBn = "আল্লাহ, তিনি ছাড়া কোনো সত্য উপাস্য নেই। তিনি চিরঞ্জীব, সর্বসত্তা ধারণকারী...",
                virtueEng = "Reciting this in the morning protects from Jinns until evening.",
                virtueBn = "সকালে পাঠ করলে সন্ধ্যা পর্যন্ত শয়তান ও জিন থেকে নিরাপদে থাকা যায়।",
                targetCount = 1,
                type = "morning"
            ),
            AzkarItem(
                id = "azkar_ikhlas",
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ هُوَ اللَّهُ أَحَدٌ...",
                pronEng = "Bismillahir-Rahmanir-Rahim. Qul Huwallahu Ahad...",
                pronBn = "বিসমিল্লাহির রহমানির রাহিম। কুল হুয়াল্লাহু আহাদ...",
                transEng = "Say: He is Allah, the One and Only...",
                transBn = "বলুন, তিনিই আল্লাহ, একক ও অদ্বিতীয়...",
                virtueEng = "Recite Surah Ikhlas, Falaq, and Nas 3 times for protection from all evils.",
                virtueBn = "৩ বার করে সুরা ইখলাস, ফালাক ও নাস পাঠ করলে সব বিপদ-আপদ থেকে রক্ষা পাওয়া যায়।",
                targetCount = 3,
                type = "morning"
            ),
            AzkarItem(
                id = "azkar_bismillah_allazi",
                arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                pronEng = "Bismillahil-lazi la yadurru ma'as-mihi shai'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
                pronBn = "বিসমিল্লাহিল্লাযী লা ইয়াদুররু মা'আসমিহী শাইউন ফিল আরদ্বি ওয়ালা ফিস সামা-ই ওয়া হুয়াস সামীউল আলীম।",
                transEng = "In the Name of Allah, with Whose Name nothing is harmed on earth nor in heaven, and He is the All-Hearing, the All-Knowing.",
                transBn = "আল্লাহর নামে, যাঁর নামের বরকতে আসমান ও যমীনের কোনো কিছুই কোনো ক্ষতি করতে পারে না, আর তিনি সর্বশ্রোতা, সর্বজ্ঞাতা।",
                virtueEng = "Recite 3 times. Whoever says it will not be afflicted by any sudden calamity.",
                virtueBn = "৩ বার পাঠ করলে সকাল বা সন্ধ্যায় কোনো আকস্মিক বিপদ বা ক্ষতি স্পর্শ করবে না।",
                targetCount = 3,
                type = "morning"
            ),
            AzkarItem(
                id = "azkar_sayyidul_istighfar",
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ...",
                pronEng = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka...",
                pronBn = "আল্লাহুম্মা আন্তা রব্বী লা ইলাহা ইল্লা আন্তা, খালাকতানী ওয়া আনা আবদুকা...",
                transEng = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant...",
                transBn = "হে আল্লাহ! আপনি আমার রব, আপনি ছাড়া কোনো সত্য ইলাহ নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা...",
                virtueEng = "Sayyidul Istighfar. If recited with conviction and dies that day, enters Paradise.",
                virtueBn = "সায়্যিদুল ইস্তিগফার। যে ব্যক্তি দিনে দৃঢ় বিশ্বাসের সাথে এটি পড়বে এবং সন্ধ্যায় মারা যাবে সে জান্নাতী হবে।",
                targetCount = 1,
                type = "morning"
            ),
            AzkarItem(
                id = "azkar_subhanallah_100",
                arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                pronEng = "Subhanallahi wa bihamdih.",
                pronBn = "সুবহানাল্লাহি ওয়া বিহামদিহী।",
                transEng = "Glory be to Allah and His is the praise.",
                transBn = "আল্লাহর পবিত্রতা ঘোষণা করছি তাঁর প্রশংসার সাথে।",
                virtueEng = "Recite 100 times. Sins forgiven even if they are like the foam of the sea.",
                virtueBn = "১০০ বার পাঠ করলে সমুদ্রের ফেনা পরিমাণ গুনাহ হলেও ক্ষমা করে দেওয়া হয়।",
                targetCount = 100,
                type = "morning"
            ),
            // Evening specific
            AzkarItem(
                id = "azkar_evening_ayat_kursi",
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ...",
                pronEng = "Allahu la ilaha illa Huwal-Hayyul-Qayyum...",
                pronBn = "আল্লাহু লা ইলাহা ইল্লা হুয়াল হাইয়্যুল কাইয়্যুম...",
                transEng = "Allah! There is no deity except Him, the Ever Living, the Sustainer of all existence...",
                transBn = "আল্লাহ, তিনি ছাড়া কোনো সত্য উপাস্য নেই। তিনি চিরঞ্জীব, সর্বসত্তা ধারণকারী...",
                virtueEng = "Reciting this in the evening protects from Jinns until morning.",
                virtueBn = "সন্ধ্যায় পাঠ করলে সকাল পর্যন্ত শয়তান ও জিন থেকে নিরাপদে থাকা যায়।",
                targetCount = 1,
                type = "evening"
            ),
            AzkarItem(
                id = "azkar_evening_three_surahs",
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ هُوَ اللَّهُ أَحَدٌ...",
                pronEng = "Bismillahir-Rahmanir-Rahim. Qul Huwallahu Ahad...",
                pronBn = "বিসমিল্লাহির রহমানির রাহিম। কুল হুয়াল্লাহু আহাদ...",
                transEng = "Say: He is Allah, the One and Only...",
                transBn = "বলুন, তিনিই আল্লাহ, একক ও অদ্বিতীয়...",
                virtueEng = "Recite 3 times in the evening. Suffices for protection against everything.",
                virtueBn = "সন্ধ্যায় ৩ বার পাঠ করলে সকল প্রকার ক্ষতি থেকে সুরক্ষার জন্য যথেষ্ট হয়।",
                targetCount = 3,
                type = "evening"
            ),
            AzkarItem(
                id = "azkar_audhu_bikalimat",
                arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                pronEng = "A'udhu bikalimatil-lahit-tammati min sharri ma khalaq.",
                pronBn = "আঊযু বিকালিমা-তিল্লাহিত তা-ম্মা-তি মিন শাররি মা খালাক।",
                transEng = "I seek refuge in the Perfect Words of Allah from the evil of what He has created.",
                transBn = "আল্লাহর পরিপূর্ণ কালিমা বা বাণীর সাহায্যে আশ্রয় চাইছি তাঁর সৃষ্টির অনিষ্ট থেকে।",
                virtueEng = "Recite 3 times. No poisonous insect or scorpion bite will harm you that night.",
                virtueBn = "সন্ধ্যায় ৩ বার পাঠ করলে ওই রাতে কোনো বিষাক্ত কীট-পতঙ্গ বা বিচ্ছু ক্ষতি করতে পারবে না।",
                targetCount = 3,
                type = "evening"
            ),
            AzkarItem(
                id = "azkar_raditu_billahi",
                arabic = "رَضِيتُ بِاللَّهِ رَبَّاً وَبِالْإِسْلَامِ دِيناً وَبِمُحَمَّدٍ نَبِيَّاً",
                pronEng = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin Nabiyya.",
                pronBn = "রাদীতু বিল্লাহি রব্বান ওয়া বিল ইসলামি দ্বীনান ওয়া বি মুহাম্মাদিন নাবিয়্যা।",
                transEng = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad as my Prophet.",
                transBn = "আমি সন্তুষ্ট হয়েছি আল্লাহকে প্রতিপালক হিসেবে, ইসলামকে দ্বীন হিসেবে এবং মুহাম্মদ (সাঃ)-কে নবী হিসেবে পেয়ে।",
                virtueEng = "Recite 3 times. Allah takes it upon Himself to please the reciter on Judgement Day.",
                virtueBn = "সন্ধ্যায় ৩ বার পাঠ করলে কিয়ামতের দিন আল্লাহ তাআলা সেই বান্দাকে সন্তুষ্ট করার দায়িত্ব নেবেন।",
                targetCount = 3,
                type = "evening"
            )
        )
    }

    var selectedTab by remember { mutableStateOf("morning") }
    val currentAzkarList = remember(selectedTab) { azkarList.filter { it.type == selectedTab } }

    // Store runtime counters
    val counts = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(selectedTab) {
        currentAzkarList.forEach { azkar ->
            counts[azkar.id] = prefs.getInt(azkar.id, 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEng) "Morning & Evening Azkar" else "সকাল-সন্ধ্যার জিকির",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Reset all counts for current tab
                        currentAzkarList.forEach {
                            counts[it.id] = 0
                            prefs.edit().putInt(it.id, 0).apply()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Counts", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBg,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark
                )
            )
        },
        containerColor = BgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selection (Morning / Evening)
            TabRow(
                selectedTabIndex = if (selectedTab == "morning") 0 else 1,
                containerColor = CardBg,
                contentColor = PrimaryGreen
            ) {
                Tab(
                    selected = selectedTab == "morning",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = "morning"
                    },
                    text = {
                        Text(
                            if (isEng) "Morning Azkar" else "সকালের জিকির",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    selectedContentColor = PrimaryGreen,
                    unselectedContentColor = TextGray
                )
                Tab(
                    selected = selectedTab == "evening",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = "evening"
                    },
                    text = {
                        Text(
                            if (isEng) "Evening Azkar" else "সন্ধ্যার জিকির",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    selectedContentColor = PrimaryGreen,
                    unselectedContentColor = TextGray
                )
            }

            // Overview banner with Gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isDarkModeGlobal) {
                                    listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8))
                                } else {
                                    listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                                }
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (selectedTab == "morning") {
                            if (isEng) "☀️ Recite morning Azkar from Fajr to sunrise to protect your day."
                            else "☀️ ফজর থেকে সূর্যোদয় পর্যন্ত সকালের জিকিরসমূহ পাঠ করা উত্তম।"
                        } else {
                            if (isEng) "🌙 Recite evening Azkar from Asr to sunset to safeguard your night."
                            else "🌙 আসর থেকে সূর্যাস্ত পর্যন্ত সন্ধ্যার জিকিরসমূহ পাঠ করা উত্তম।"
                        },
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }

            // Lazy Column of Azkar Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentAzkarList, key = { it.id }) { azkar ->
                    val currentCount = counts[azkar.id] ?: 0
                    val isCompleted = currentCount >= azkar.targetCount

                    val cardBgColor by animateColorAsState(
                        targetValue = if (isCompleted) {
                            if (isDarkModeGlobal) Color(0xFF022C22) else Color(0xFFECFDF5)
                        } else {
                            CardBg
                        },
                        label = "cardBgColor"
                    )

                    val borderStrokeColor by animateColorAsState(
                        targetValue = if (isCompleted) {
                            PrimaryGreen
                        } else {
                            if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)
                        },
                        label = "borderColor"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (currentCount < azkar.targetCount) {
                                    val nextCount = currentCount + 1
                                    counts[azkar.id] = nextCount
                                    prefs.edit().putInt(azkar.id, nextCount).apply()
                                    if (nextCount == azkar.targetCount) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, borderStrokeColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Top Row: Count Display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isCompleted) PrimaryGreen else (if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFF1F5F9)),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${if (isEng) "Count: " else "সংখ্যা: "} $currentCount / ${azkar.targetCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color.White else TextDark
                                    )
                                }

                                // Interactive Counter Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) PrimaryGreen else PrimaryGreen.copy(alpha = 0.15f))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (currentCount < azkar.targetCount) {
                                                val nextCount = currentCount + 1
                                                counts[azkar.id] = nextCount
                                                prefs.edit().putInt(azkar.id, nextCount).apply()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = "+1",
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Arabic text
                            Text(
                                text = azkar.arabic,
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Pronunciation
                            Text(
                                text = if (isEng) azkar.pronEng else azkar.pronBn,
                                fontSize = 12.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Translation
                            Text(
                                text = if (isEng) azkar.transEng else azkar.transBn,
                                fontSize = 12.sp,
                                color = TextDark.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Divider(color = (if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)).copy(alpha = 0.5f))

                            Spacer(modifier = Modifier.height(8.dp))

                            // Virtue / Fazilat
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = if (isEng) "Virtue: " else "ফজিলত: ",
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isEng) azkar.virtueEng else azkar.virtueBn,
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
