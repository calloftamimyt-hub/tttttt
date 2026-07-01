package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

data class MasailItem(
    val id: String,
    val questionEng: String,
    val questionBn: String,
    val answerEng: String,
    val answerBn: String,
    val refEng: String,
    val refBn: String,
    val category: String // "taharah", "salat", "sawm", "family", "business"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish

    // Curated list of common rules and fatwas
    val masailList = remember {
        listOf(
            MasailItem(
                id = "masail_wudu_flowing_blood",
                questionEng = "Does bleeding break Wudu?",
                questionBn = "শরীরের কোনো অংশ থেকে রক্ত প্রবাহিত হলে কি ওযু ভেঙে যায়?",
                answerEng = "According to the Hanafi school, flowing blood from the body (which spreads past the point of exit) breaks Wudu. According to the Shafi'i school, bleeding does not break Wudu unless it exits from the private parts.",
                answerBn = "হানাফী মাযহাব অনুযায়ী শরীর থেকে রক্ত বের হয়ে যদি অপবিত্র স্থানে ছড়িয়ে পড়ে বা গড়িয়ে পড়ে, তবে ওযু ভেঙে যাবে। তবে শাফেয়ী মাযহাব মতে লজ্জাস্থান ছাড়া অন্য স্থান দিয়ে রক্ত প্রবাহিত হলে ওযু ভাঙে না।",
                refEng = "Al-Hidayah, Fatawa Hindiyyah",
                refBn = "আল-হেদায়া ১/১৮, ফাতাওয়ায়ে আলমগিরী ১/১০",
                category = "taharah"
            ),
            MasailItem(
                id = "masail_makeup_wudu",
                questionEng = "Is Wudu valid over makeup and nail polish?",
                questionBn = "মেকআপ এবং নেইলপলিশ লাগানো অবস্থায় ওযু করলে কি ওযু হবে?",
                answerEng = "If the makeup or nail polish forms an waterproof barrier that prevents water from contacting the skin or nail surface directly, Wudu is NOT valid. Water-permeable makeup is fine, but standard nail polish must be completely removed before making Wudu.",
                answerBn = "নেইলপলিশ বা এমন কোনো মেকআপ যদি নখ বা ত্বকের ওপর একটি পানিনিরোধক স্তর তৈরি করে যার ফলে পানি ভেতরে পৌঁছাতে পারে না, তবে ওযু হবে না। ওযু করার পূর্বে সাধারণ নেইলপলিশ এবং ওয়াটারপ্রুফ মেকআপ সম্পূর্ণ তুলে ফেলা আবশ্যক।",
                refEng = "Fatawa Darul Uloom Deoband",
                refBn = "ফাতাওয়ায়ে দারুল উলুম ১/২৯৪",
                category = "taharah"
            ),
            MasailItem(
                id = "masail_forget_surah",
                questionEng = "What should I do if I forget to recite a Surah after Surah Al-Fatiha in Salat?",
                questionBn = "ফরজ নামাজের প্রথম দুই রাকাতে সূরা ফাতিহার পর সূরা মেলাতে ভুলে গেলে করণীয় কী?",
                answerEng = "If you forget to recite a Surah in the first two Rak'ats of Fard prayer, you must perform Sajdah Sahw (two prostrations of forgetfulness) at the end of the prayer. If you completely forget Sajdah Sahw and complete Salat, the prayer must be repeated.",
                answerBn = "ফরজ নামাজের প্রথম দুই রাকাতে ভুলবশত সূরা ফাতিহার পর অন্য সূরা মেলাতে ভুলে গেলে, নামাজের শেষে 'সাহু সেজদা' (সেজদা সাহু) আদায় করা ওয়াজিব। সাহু সেজদা না করলে নামাজ পুনরায় পড়তে হবে।",
                refEng = "Sahih al-Bukhari 1224, Al-Bahrur Raiq",
                refBn = "সহীহ বুখারী ১২২৪, আল-বাহরুর রায়েক ২/১০২",
                category = "salat"
            ),
            MasailItem(
                id = "masail_swallow_saliva",
                questionEng = "Does swallowing saliva break the fast?",
                questionBn = "রোজা রাখা অবস্থায় নিজের থুথু গিললে কি রোজা ভেঙে যায়?",
                answerEng = "No, swallowing your own normal saliva does not break the fast under any circumstances, as it is impossible to avoid. However, intentionally gathering saliva in the mouth and swallowing it is disliked (Makruh) but still does not break the fast.",
                answerBn = "না, স্বাভাবিক অবস্থায় নিজের থুথু বা লালা গিললে রোজা ভাঙে না। কারণ এটি পরিহার করা অসম্ভব এবং এটি মানব শরীরের ভেতরের স্বাভাবিক অংশ। তবে মুখ গহ্বরে ইচ্ছাকৃতভাবে অনেক থুথু জমা করে একবারে গিলে ফেলা মকরূহ, কিন্তু এতেও রোজা ভাঙবে না।",
                refEng = "Fatawa Hindiyyah 1/199",
                refBn = "ফাতাওয়ায়ে আলমগিরী ১/১৯৯, কিতাবুল ফিকহ ১/৯১০",
                category = "sawm"
            ),
            MasailItem(
                id = "masail_vomit_fasting",
                questionEng = "Does vomiting break the fast?",
                questionBn = "রোজা রেখে বমি করলে কি রোজা ভেঙে যায়?",
                answerEng = "If vomiting occurs naturally and unintentionally, the fast is NOT broken, regardless of the amount. However, if a person intentionally induces vomiting and it fills the mouth (mouthful), the fast is broken and must be made up (Qada).",
                answerBn = "অনিচ্ছাকৃতভাবে বমি হলে রোজা ভাঙে না, বমি যত বেশিই হোক না কেন। তবে কেউ যদি রোজা রাখা অবস্থায় ইচ্ছাকৃতভাবে মুখ ভরে বমি করে, তবে তার রোজা ভেঙে যাবে এবং পরবর্তীতে সেই রোজার শুধু কাজা আদায় করতে হবে (কাফফারা লাগবে না)।",
                refEng = "Jami` at-Tirmidhi 720",
                refBn = "জামে তিরমিযী ৭২০, সুনান আবু দাউদ ২৩৮০",
                category = "sawm"
            ),
            MasailItem(
                id = "masail_husband_wife_names",
                questionEng = "Is it forbidden for a wife to call her husband by name?",
                questionBn = "স্ত্রী কর্তৃক স্বামীকে নাম ধরে ডাকা কি শরীয়তে নিষেধ?",
                answerEng = "It is not haram (strictly forbidden) for a wife to call her husband by his name. However, in many Muslim cultures, doing so is considered disrespectful or rude. Thus, it is recommended to use affectionate titles or respectful terms of address instead.",
                answerBn = "স্ত্রী কর্তৃক স্বামীকে সরাসরি নাম ধরে ডাকা হারাম বা গুনাহের কাজ নয়। তবে সামাজিক প্রথা এবং পারস্পরিক শ্রদ্ধাবোধের খাতিরে আমাদের উপমহাদেশে নাম ধরে ডাকা অনুচিত বা বেয়াদবি মনে করা হয়। তাই সম্মানজনক কোনো সম্বোধন করাই উত্তম ও সুন্নাতসম্মত।",
                refEng = "Radd al-Muhtar (Ibn Abidin)",
                refBn = "ফাতাওয়ায়ে শামী ৬/৪১৮",
                category = "family"
            ),
            MasailItem(
                id = "masail_interest_bank_job",
                questionEng = "Is it permissible to work in a bank that deals with interest?",
                questionBn = "সুদ ভিত্তিক ব্যাংকে চাকুরী করার ব্যাপারে ইসলামের হুকুম কী?",
                answerEng = "Any transaction involving interest (Riba) is strictly forbidden in Islam. Consequently, working in a role that directly writes down, witnesses, calculates, or processes interest transactions is impermissible (Haram). Muslims are advised to seek pure, halal livelihoods.",
                answerBn = "ইসলামে যেকোনো ধরণের সুদের আদান-প্রদান, হিসাব রাখা বা সুদে সহযোগিতা করা কঠোরভাবে হারাম। তাই সুদভিত্তিক ব্যাংকে যেখানে সরাসরি সুদের হিসাব নিকাশ লিখতে হয় বা চুক্তি করতে হয়, সেখানে চাকরি করা জায়েজ নয়। হালাল উপার্জনের চেষ্টা করা ওয়াজিব।",
                refEng = "Sahih Muslim 1598",
                refBn = "সহীহ মুসলিম ১৫৯৮ (সুদদাতা, সুদের লেখক ও সাক্ষী সবার ওপর অভিশাপ)",
                category = "business"
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Instant filter logic
    val filteredMasail = remember(searchQuery, selectedCategory) {
        masailList.filter { item ->
            val matchCategory = selectedCategory == "all" || item.category == selectedCategory
            val matchSearch = if (searchQuery.trim().isEmpty()) true else {
                item.questionEng.contains(searchQuery, ignoreCase = true) ||
                item.questionBn.contains(searchQuery, ignoreCase = true) ||
                item.answerEng.contains(searchQuery, ignoreCase = true) ||
                item.answerBn.contains(searchQuery, ignoreCase = true)
            }
            matchCategory && matchSearch
        }
    }

    val categories = remember {
        listOf(
            "all" to (if (isEng) "All" else "সব"),
            "taharah" to (if (isEng) "Taharah" else "পবিত্রতা"),
            "salat" to (if (isEng) "Salat" else "নামাজ"),
            "sawm" to (if (isEng) "Sawm" else "রোজা"),
            "family" to (if (isEng) "Family" else "পরিবার"),
            "business" to (if (isEng) "Business" else "লেনদেন")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEng) "Islamic Masa'il & Fatwa" else "ইসলামিক মাসয়ালা-মাসায়েল",
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
            // Premium Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text(
                        if (isEng) "Search masa'il or keywords..." else "মাসয়ালা বা কি-ওয়ার্ড দিয়ে খুঁজুন...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0),
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedLabelColor = PrimaryGreen,
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Chips List (Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
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

            // Results List
            if (filteredMasail.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEng) "No rulings found" else "কোনো মাসয়ালা পাওয়া যায়নি",
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isEng) "Try searching other keywords" else "অন্য কোনো কি-ওয়ার্ড দিয়ে পুনরায় চেষ্টা করুন",
                            color = TextGray.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMasail, key = { it.id }) { item ->
                        val isExpanded = expandedStates[item.id] ?: false
                        val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow")

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expandedStates[item.id] = !isExpanded
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bullet indicator
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(PrimaryGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isEng) item.questionEng else item.questionBn,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = TextGray,
                                        modifier = Modifier
                                            .rotate(arrowRotation)
                                            .size(24.dp)
                                    )
                                }

                                // Answer collapsible region
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        Divider(
                                            color = if (isDarkModeGlobal) Color(0xFF334155) else Color(
                                                0xFFF1F5F9
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = if (isEng) item.answerEng else item.answerBn,
                                            fontSize = 13.sp,
                                            color = TextDark.copy(alpha = 0.9f),
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Reference row and Copy action
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isEng) "Source / Evidence:" else "দলিল ও নির্ভরযোগ্য সূত্র:",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryGreen
                                                )
                                                Text(
                                                    text = if (isEng) item.refEng else item.refBn,
                                                    fontSize = 10.sp,
                                                    color = TextGray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            // Copy button
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isDarkModeGlobal) Color(0xFF1E293B) else Color(
                                                            0xFFF1F5F9
                                                        )
                                                    )
                                                    .clickable {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                        val clipboard =
                                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText(
                                                            "Fatwa",
                                                            if (isEng) item.answerEng else item.answerBn
                                                        )
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                if (isEng) "Copied answer to clipboard" else "মাসয়ালা কপি করা হয়েছে",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = PrimaryGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
