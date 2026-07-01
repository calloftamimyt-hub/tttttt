package com.example

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

data class SunnahAct(
    val id: String,
    val titleEng: String,
    val titleBn: String,
    val descEng: String,
    val descBn: String,
    val refEng: String,
    val refBn: String,
    val category: String // "morning", "night", "prayer", "social"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySunnahScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish
    val prefs = remember { context.getSharedPreferences("daily_sunnah_prefs", Context.MODE_PRIVATE) }

    // Hardcoded List of Sunnah acts
    val sunnahList = remember {
        listOf(
            SunnahAct(
                id = "sunnah_wake_up",
                titleEng = "Using Miswak upon waking up",
                titleBn = "ঘুম থেকে উঠে মেসওয়াক করা",
                descEng = "Whenever the Prophet (ﷺ) got up at night, he used to clean his mouth with a Siwak.",
                descBn = "আল্লাহর রাসূল (সাঃ) রাতে যখনই ঘুম থেকে উঠতেন, তখনই মেসওয়াক দিয়ে মুখ পরিষ্কার করতেন।",
                refEng = "Sahih al-Bukhari 245",
                refBn = "সহীহ বুখারী ২৪৫",
                category = "morning"
            ),
            SunnahAct(
                id = "sunnah_right_side",
                titleEng = "Sleeping on the right side",
                titleBn = "ডান কাতে ঘুমানো",
                descEng = "Lie down on your right side when going to sleep.",
                descBn = "ঘুমানোর সময় প্রথমে ডান কাতে শুয়ে পড়া সুন্নাত।",
                refEng = "Sahih al-Bukhari 247",
                refBn = "সহীহ বুখারী ২৪৭",
                category = "night"
            ),
            SunnahAct(
                id = "sunnah_bismillah",
                titleEng = "Saying Bismillah before eating",
                titleBn = "খাবারের শুরুতে বিসমিল্লাহ বলা",
                descEng = "Say Bismillah and eat with your right hand.",
                descBn = "খাবারের শুরুতে 'বিসমিল্লাহ' বলা এবং ডান হাত দিয়ে খাওয়া সুন্নাত।",
                refEng = "Sahih al-Bukhari 5376",
                refBn = "সহীহ বুখারী ৫৩৭৬",
                category = "social"
            ),
            SunnahAct(
                id = "sunnah_three_gulps",
                titleEng = "Drinking water in three breaths",
                titleBn = "তিন নিঃশ্বাসে পানি পান করা",
                descEng = "Do not drink water in a single breath, rather drink in two or three breaths.",
                descBn = "এক নিঃশ্বাসে সব পানি পান না করে, দুই বা তিন নিঃশ্বাসে পানি পান করা সুন্নাত।",
                refEng = "Sahih Muslim 2028",
                refBn = "সহীহ মুসলিম ২০২৮",
                category = "social"
            ),
            SunnahAct(
                id = "sunnah_shake_hands",
                titleEng = "Shaking hands on meeting",
                titleBn = "সাক্ষাৎ হলে মুসাফাহা করা",
                descEng = "Two Muslims do not meet and shake hands but their sins are forgiven before they part.",
                descBn = "দুজন মুসলিম মিলিত হয়ে মুসাফাহা (করমর্দন) করলে আলাদা হওয়ার আগেই তাদের গুনাহ ক্ষমা করে দেওয়া হয়।",
                refEng = "Sunan Abi Dawud 5212",
                refBn = "সুনান আবু দাউদ ৫২১২",
                category = "social"
            ),
            SunnahAct(
                id = "sunnah_siwak_wudu",
                titleEng = "Using Siwak during Wudu",
                titleBn = "ওযুর সময় মেসওয়াক করা",
                descEng = "If it were not for overburdening my Ummah, I would order them to use Siwak with every Wudu.",
                descBn = "আমি যদি আমার উম্মতের উপর কষ্টকর মনে না করতাম, তবে প্রত্যেক ওযুর সময় মেসওয়াক করার নির্দেশ দিতাম।",
                refEng = "Sahih al-Bukhari 887",
                refBn = "সহীহ বুখারী ৮৮৭",
                category = "prayer"
            ),
            SunnahAct(
                id = "sunnah_enter_mosque",
                titleEng = "Entering Mosque with right foot",
                titleBn = "ডান পা দিয়ে মসজিদে প্রবেশ করা",
                descEng = "It is Sunnah to enter the mosque with the right foot first.",
                descBn = "মসজিদে ডান পা দিয়ে প্রবেশ করা এবং বের হওয়ার সময় বাম পা দিয়ে বের হওয়া সুন্নাত।",
                refEng = "Al-Hakim 1/218",
                refBn = "আল-হাকিম ১/২১৮",
                category = "prayer"
            ),
            SunnahAct(
                id = "sunnah_smile",
                titleEng = "Smiling at others",
                titleBn = "মুচকি হাসি হাসা",
                descEng = "Your smiling in the face of your brother is charity (Sadaqah).",
                descBn = "তোমার ভাইয়ের মুখের সামনে তোমার মুচকি হাসি হাসা একটি সদকা স্বরূপ।",
                refEng = "Jami` at-Tirmidhi 1956",
                refBn = "জামে তিরমিযী ১৯৫৬",
                category = "social"
            )
        )
    }

    // Load completed states
    val completedStates = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) {
        sunnahList.forEach { act ->
            completedStates[act.id] = prefs.getBoolean(act.id, false)
        }
    }

    // Filter categories
    var selectedCategory by remember { mutableStateOf("all") }
    val categories = remember {
        listOf(
            "all" to (if (isEng) "All" else "সব"),
            "morning" to (if (isEng) "Morning" else "সকাল"),
            "night" to (if (isEng) "Night" else "রাত"),
            "prayer" to (if (isEng) "Prayer" else "নামাজ"),
            "social" to (if (isEng) "Social" else "সামাজিক")
        )
    }

    val filteredList = sunnahList.filter {
        selectedCategory == "all" || it.category == selectedCategory
    }

    val totalCount = sunnahList.size
    val completedCount = completedStates.values.count { it }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEng) "Daily Sunnah Acts" else "দৈনিক সুন্নাহসমূহ",
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
            // Premium Progress Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (isDarkModeGlobal) {
                                    listOf(Color(0xFF064E3B), Color(0xFF115E59))
                                } else {
                                    listOf(Color(0xFFECFDF5), Color(0xFFCCFBF1))
                                }
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (isEng) "Your Daily Sunnah Progress" else "আপনার আজকের সুন্নাহ অগ্রগতি",
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isEng) "$completedCount of $totalCount completed" else "$completedCount টি সুন্নাহ সম্পন্ন হয়েছে (মোট $totalCount টি)",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = PrimaryGreen,
                            trackColor = if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEng) "Reviving a Sunnah earns the reward of 100 martyrs." else "একটি সুন্নাত জীবিত করলে ১০০ শহীদের সওয়াব পাওয়া যায়।",
                                color = TextDark.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Categories horizontal scroll list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catId, catTitle) ->
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) PrimaryGreen else (if (isDarkModeGlobal) Color(0xFF1E293B) else Color(
                                    0xFFE2E8F0
                                ))
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedCategory = catId
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = catTitle,
                            color = if (isSelected) Color.White else TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // List of Sunnahs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { act ->
                    val isChecked = completedStates[act.id] ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newState = !isChecked
                                completedStates[act.id] = newState
                                prefs.edit().putBoolean(act.id, newState).apply()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) {
                                if (isDarkModeGlobal) Color(0xFF022C22) else Color(0xFFF0FDF4)
                            } else {
                                CardBg
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isChecked) PrimaryGreen.copy(alpha = 0.4f) else (if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isEng) act.titleEng else act.titleBn,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val newState = !isChecked
                                        completedStates[act.id] = newState
                                        prefs.edit().putBoolean(act.id, newState).apply()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = "Toggle Complete",
                                        tint = if (isChecked) PrimaryGreen else TextGray.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isEng) act.descEng else act.descBn,
                                fontSize = 12.sp,
                                color = TextDark.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${if (isEng) "Reference: " else "সূত্র: "}${if (isEng) act.refEng else act.refBn}",
                                fontSize = 10.sp,
                                color = TextGray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
