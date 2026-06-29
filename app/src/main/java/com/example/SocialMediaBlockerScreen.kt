package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Block
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaBlockerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }
    val isEng = GlobalLanguage.isEnglish

    // Scroll state
    val scrollState = rememberScrollState()

    // Helper function to create state and trigger update
    @Composable
    fun blockState(key: String): MutableState<Boolean> {
        val state = remember { mutableStateOf(sharedPrefs.getBoolean(key, false)) }
        return state
    }

    // Platforms definitions
    val ytLong = blockState("yt_long_blocked")
    val ytReels = blockState("yt_reels_blocked")
    val ytSearch = blockState("yt_search_blocked")
    val ytEntire = blockState("yt_entire_blocked")

    val fbApp = blockState("fb_app_blocked")
    val fbStory = blockState("fb_story_blocked")
    val fbSearch = blockState("fb_search_blocked")
    val fbReels = blockState("fb_reels_blocked")
    val fbEntire = blockState("fb_entire_blocked")

    val tgApp = blockState("tg_app_blocked")
    val tgSearch = blockState("tg_search_blocked")
    val tgStory = blockState("tg_story_blocked")
    val tgEntire = blockState("tg_entire_blocked")

    val waApp = blockState("wa_app_blocked")
    val waStory = blockState("wa_story_blocked")
    val waEntire = blockState("wa_entire_blocked")

    val msApp = blockState("ms_app_blocked")
    val msStory = blockState("ms_story_blocked")
    val msEntire = blockState("ms_entire_blocked")

    val igApp = blockState("ig_app_blocked")
    val igSearch = blockState("ig_search_blocked")
    val igReels = blockState("ig_reels_blocked")
    val igFeatures = blockState("ig_features_blocked")
    val igEntire = blockState("ig_entire_blocked")

    // Master list to update parent "social_blocked" key
    val allStates = listOf(
        ytLong, ytReels, ytSearch, ytEntire,
        fbApp, fbStory, fbSearch, fbReels, fbEntire,
        tgApp, tgSearch, tgStory, tgEntire,
        waApp, waStory, waEntire,
        msApp, msStory, msEntire,
        igApp, igSearch, igReels, igFeatures, igEntire
    )

    val updateMasterState = {
        val isAnyBlocked = allStates.any { it.value }
        sharedPrefs.edit().putBoolean("social_blocked", isAnyBlocked).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEng) "Social Media Blocker" else "সোশ্যাল মিডিয়া ব্লকার",
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
            // Header Intro Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AppBlocking,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = if (isEng) "Focus Mode & Protection" else "ফোকাস মোড ও আত্মরক্ষা",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = if (isEng) "Block distracting elements of social media to secure your time." 
                                   else "আপনার মূল্যবান সময় ও আমল সুরক্ষায় সোশ্যাল মিডিয়ার অপ্রয়োজনীয় ডিস্ট্রাকশন ব্লক করুন।",
                            fontSize = 11.5.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }

            // Platform 1: YouTube
            PlatformBlockerCard(
                title = "YouTube",
                themeColor = Color(0xFFEF4444),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("yt_long_blocked", if (isEng) "Block Long Videos" else "লং ভিডিও ব্লক", ytLong),
                    BlockerOptionData("yt_reels_blocked", if (isEng) "Block Shorts/Reels" else "ইউটিউব শর্টস ব্লক", ytReels),
                    BlockerOptionData("yt_search_blocked", if (isEng) "Block Search" else "ইউটিউব সার্চ ব্লক", ytSearch),
                    BlockerOptionData("yt_entire_blocked", if (isEng) "Block Entire YouTube" else "সম্পূর্ণ ইউটিউব ব্লক", ytEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform 2: Facebook
            PlatformBlockerCard(
                title = "Facebook",
                themeColor = Color(0xFF1877F2),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("fb_app_blocked", if (isEng) "Block News Feed" else "ফেসবুক নিউজফিড ব্লক", fbApp),
                    BlockerOptionData("fb_story_blocked", if (isEng) "Block Stories" else "ফেসবুক স্টোরি ব্লক", fbStory),
                    BlockerOptionData("fb_search_blocked", if (isEng) "Block Search" else "ফেসবুক সার্চ ব্লক", fbSearch),
                    BlockerOptionData("fb_reels_blocked", if (isEng) "Block Facebook Reels" else "ফেসবুক রিলস ব্লক", fbReels),
                    BlockerOptionData("fb_entire_blocked", if (isEng) "Block Entire Facebook" else "সম্পূর্ণ ফেসবুক ব্লক", fbEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform 3: Instagram
            PlatformBlockerCard(
                title = "Instagram",
                themeColor = Color(0xFFE1306C),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("ig_app_blocked", if (isEng) "Block Feed" else "ইনস্টাগ্রাম ফিড ব্লক", igApp),
                    BlockerOptionData("ig_search_blocked", if (isEng) "Block Explore/Search" else "এক্সপ্লোর ও সার্চ ব্লক", igSearch),
                    BlockerOptionData("ig_reels_blocked", if (isEng) "Block Instagram Reels" else "ইনস্টাগ্রাম রিলস ব্লক", igReels),
                    BlockerOptionData("ig_features_blocked", if (isEng) "Block Stories & Messaging" else "স্টোরি ও মেসেজিং ব্লক", igFeatures),
                    BlockerOptionData("ig_entire_blocked", if (isEng) "Block Entire Instagram" else "সম্পূর্ণ ইনস্টাগ্রাম ব্লক", igEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform 4: Telegram
            PlatformBlockerCard(
                title = "Telegram",
                themeColor = Color(0xFF24A1DE),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("tg_app_blocked", if (isEng) "Block App Access" else "অ্যাপ অ্যাক্সেস ব্লক", tgApp),
                    BlockerOptionData("tg_search_blocked", if (isEng) "Block Global Search" else "গ্লোবাল সার্চ ব্লক", tgSearch),
                    BlockerOptionData("tg_story_blocked", if (isEng) "Block Stories" else "টেলিগ্রাম স্টোরি ব্লক", tgStory),
                    BlockerOptionData("tg_entire_blocked", if (isEng) "Block Entire Telegram" else "সম্পূর্ণ টেলিগ্রাম ব্লক", tgEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform 5: WhatsApp
            PlatformBlockerCard(
                title = "WhatsApp",
                themeColor = Color(0xFF25D366),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("wa_app_blocked", if (isEng) "Block Chats Access" else "চ্যাট অ্যাক্সেস ব্লক", waApp),
                    BlockerOptionData("wa_story_blocked", if (isEng) "Block WhatsApp Status/Stories" else "স্ট্যাটাস ও স্টোরি ব্লক", waStory),
                    BlockerOptionData("wa_entire_blocked", if (isEng) "Block Entire WhatsApp" else "সম্পূর্ণ হোয়াটসঅ্যাপ ব্লক", waEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform 6: Messenger
            PlatformBlockerCard(
                title = "Messenger",
                themeColor = Color(0xFF00B2FF),
                isEng = isEng,
                options = listOf(
                    BlockerOptionData("ms_app_blocked", if (isEng) "Block Chats" else "চ্যাট উইন্ডো ব্লক", msApp),
                    BlockerOptionData("ms_story_blocked", if (isEng) "Block Stories" else "মেসেঞ্জার স্টোরি ব্লক", msStory),
                    BlockerOptionData("ms_entire_blocked", if (isEng) "Block Entire Messenger" else "সম্পূর্ণ মেসেঞ্জার ব্লক", msEntire)
                ),
                sharedPrefs = sharedPrefs,
                onStateChanged = updateMasterState
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

data class BlockerOptionData(
    val key: String,
    val label: String,
    val state: MutableState<Boolean>
)

@Composable
fun PlatformBlockerCard(
    title: String,
    themeColor: Color,
    isEng: Boolean,
    options: List<BlockerOptionData>,
    sharedPrefs: android.content.SharedPreferences,
    onStateChanged: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with colored indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 10.dp)
                            .background(themeColor, shape = RoundedCornerShape(5.dp))
                    )
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                val activeCount = options.count { it.state.value }
                if (activeCount > 0) {
                    Text(
                        text = if (isEng) "$activeCount Active" else "${activeCount}টি সচল",
                        fontSize = 11.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(themeColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Blocker Items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (option.state.value) themeColor else Color(0xFF475569)
                        )
                        Switch(
                            checked = option.state.value,
                            onCheckedChange = { checked ->
                                option.state.value = checked
                                sharedPrefs.edit().putBoolean(option.key, checked).apply()
                                onStateChanged()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = themeColor,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }
        }
    }
}
