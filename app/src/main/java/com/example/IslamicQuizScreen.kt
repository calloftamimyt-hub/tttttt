package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

data class QuizQuestion(
    val questionEng: String,
    val questionBn: String,
    val optionsEng: List<String>,
    val optionsBn: List<String>,
    val correctIndex: Int,
    val explanationEng: String,
    val explanationBn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicQuizScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish

    // Predefined 8 rich questions
    val questions = remember {
        listOf(
            QuizQuestion(
                questionEng = "Which Prophet built the Ark by the command of Allah?",
                questionBn = "আল্লাহর নির্দেশে কোন নবী নৌকা বা কিশতি তৈরি করেছিলেন?",
                optionsEng = listOf("Prophet Musa (AS)", "Prophet Nuh (AS)", "Prophet Ibrahim (AS)", "Prophet Isa (AS)"),
                optionsBn = listOf("হযরত মুসা (আঃ)", "হযরত নূহ (আঃ)", "হযরত ইব্রাহিম (আঃ)", "হযরত ঈসা (আঃ)"),
                correctIndex = 1,
                explanationEng = "Prophet Nuh (AS) built the Ark under Allah's precise guidance to save the believers from the great flood.",
                explanationBn = "হযরত নূহ (আঃ) মহা প্লাবন থেকে ঈমানদারদের বাঁচাতে আল্লাহর নির্দেশে বিশাল একটি কিশতি বা নৌকা তৈরি করেছিলেন।"
            ),
            QuizQuestion(
                questionEng = "In which cave did the Prophet Muhammad (ﷺ) receive the first revelation?",
                questionBn = "রাসূলুল্লাহ (সাঃ) কোন গুহায় প্রথম ওহী লাভ করেছিলেন?",
                optionsEng = listOf("Cave of Thawr", "Cave of Hira", "Cave of Kahf", "Cave of Uhud"),
                optionsBn = listOf("সওর গুহা", "হেরা গুহা", "কাহাফ গুহা", "ওহুদ গুহা"),
                correctIndex = 1,
                explanationEng = "The first revelation of the Holy Quran was revealed to Prophet Muhammad (ﷺ) in the Cave of Hira on Mount Jabal al-Nour.",
                explanationBn = "জাবালে নূর পাহাড়ে অবস্থিত হেরা গুহায় রাসূলুল্লাহ (সাঃ) এর নিকট জিবরাঈল (আঃ) এর মাধ্যমে পবিত্র কুরআনের প্রথম ওহী নাজিল হয়েছিল।"
            ),
            QuizQuestion(
                questionEng = "How many Surahs are there in the Holy Quran?",
                questionBn = "পবিত্র কুরআনে মোট কতটি সূরা রয়েছে?",
                optionsEng = listOf("112", "113", "114", "115"),
                optionsBn = listOf("১১২", "১১৩", "১১৪", "১১৫"),
                correctIndex = 2,
                explanationEng = "The Quran is divided into 114 Surahs (chapters), composed of Makki and Madani revelations.",
                explanationBn = "পবিত্র কুরআনে মোট ১১৪টি সূরা রয়েছে, যার মধ্যে কিছু মক্কী ও কিছু মাদানী সূরা।"
            ),
            QuizQuestion(
                questionEng = "Who was the first Caliph of Islam?",
                questionBn = "ইসলামের প্রথম খলিফা কে ছিলেন?",
                optionsEng = listOf("Umar ibn al-Khattab (RA)", "Ali ibn Abi Talib (RA)", "Uthman ibn Affan (RA)", "Abu Bakr as-Siddiq (RA)"),
                optionsBn = listOf("হযরত উমর ইবনুল খাত্তাব (রাঃ)", "হযরত আলী ইবনে আবী তালিব (রাঃ)", "হযরত উসমান ইবনে আফফান (রাঃ)", "হযরত আবু বকর আস-সিদ্দিক (রাঃ)"),
                correctIndex = 3,
                explanationEng = "Abu Bakr as-Siddiq (RA) was selected as the first Caliph (successor) of the Muslim community after the Prophet (ﷺ)'s passing.",
                explanationBn = "রাসূলুল্লাহ (সাঃ) এর ইন্তেকালের পর মুসলমানদের প্রথম খলিফা হিসেবে হযরত আবু বকর আস-সিদ্দিক (রাঃ) মনোনীত হন।"
            ),
            QuizQuestion(
                questionEng = "Which battle is known as the first major battle of Islam?",
                questionBn = "কোন যুদ্ধটি ইসলামের প্রথম বড় ও প্রধান যুদ্ধ হিসেবে পরিচিত?",
                optionsEng = listOf("Battle of Uhud", "Battle of Badr", "Battle of Trench", "Battle of Khaibar"),
                optionsBn = listOf("ওহুদের যুদ্ধ", "বদরের যুদ্ধ", "খন্দকের যুদ্ধ", "খায়বারের যুদ্ধ"),
                correctIndex = 1,
                explanationEng = "The Battle of Badr, fought in 2 AH, was the first decisive battle where 313 Muslims defeated a much larger Quraysh army.",
                explanationBn = "হিজরি ২ সনে সংঘটিত বদরের যুদ্ধ ছিল ইসলামের প্রথম বড় যুদ্ধ, যেখানে মাত্র ৩১৩ জন মুসলিম বিশাল কুরাইশ বাহিনীকে পরাজিত করেছিলেন।"
            ),
            QuizQuestion(
                questionEng = "Which pillar of Islam is performed during the month of Ramadan?",
                questionBn = "রমজান মাসে ইসলামের কোন গুরুত্বপূর্ণ রুকন বা স্তম্ভ পালন করা হয়?",
                optionsEng = listOf("Salah", "Zakat", "Sawm (Fasting)", "Hajj"),
                optionsBn = listOf("নামাজ", "যাকাত", "রোজা (সওম)", "হজ"),
                correctIndex = 2,
                explanationEng = "Sawm (Fasting) during the holy month of Ramadan is the fourth pillar of Islam, mandatory for every healthy Muslim.",
                explanationBn = "রমজান মাসের রোজা রাখা ইসলামের চতুর্থ স্তম্ভ, যা প্রত্যেক প্রাপ্তবয়স্ক সুস্থ মুসলমানের জন্য আবশ্যক।"
            ),
            QuizQuestion(
                questionEng = "Which Prophet is known for his exemplary patience during severe illness?",
                questionBn = "কোন নবী তাঁর কঠিন অসুস্থতায় চরম ধৈর্যের জন্য ইতিহাসে বিখ্যাত?",
                optionsEng = listOf("Prophet Yusuf (AS)", "Prophet Ayyub (AS)", "Prophet Yunus (AS)", "Prophet Yaqub (AS)"),
                optionsBn = listOf("হযরত ইউসুফ (আঃ)", "হযরত আইয়ুব (আঃ)", "হযরত ইউনুস (আঃ)", "হযরত ইয়াকুব (আঃ)"),
                correctIndex = 1,
                explanationEng = "Prophet Ayyub (AS) underwent severe trials of disease and loss but maintained steadfast patience and gratitude to Allah.",
                explanationBn = "হযরত আইয়ুব (আঃ) দীর্ঘকাল মারাত্মক রোগ এবং সর্বস্ব হারানোর মাধ্যমে আল্লাহর কঠিন পরীক্ষায় পড়েছিলেন, কিন্তু তিনি চরম ধৈর্য ধারণ করেছিলেন।"
            ),
            QuizQuestion(
                questionEng = "What is the name of the fountain in Paradise promised to Prophet Muhammad (ﷺ)?",
                questionBn = "জান্নাতে রাসূলুল্লাহ (সাঃ)-কে দান করা হাউজের ঝর্ণার নাম কী?",
                optionsEng = listOf("Zamzam", "Salsabil", "Kauthar", "Tasnim"),
                optionsBn = listOf("জমজম", "সালসাবিল", "কাওসার", "তাসনিম"),
                correctIndex = 2,
                explanationEng = "Al-Kauthar is a bountiful river and pool in Paradise gifted by Allah to the Prophet (ﷺ) to quench the thirst of his believers.",
                explanationBn = "কাওসার হলো জান্নাতের একটি বরকতময় নহর ও ঝর্ণা, যা আল্লাহ তাআলা কিয়ামতের দিন তাঁর উম্মতদের তৃষ্ণা মেটানোর জন্য প্রিয় নবীজিকে উপহার দিয়েছেন।"
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var answerSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentIndex]

    fun resetQuiz() {
        currentIndex = 0
        selectedOptionIndex = null
        answerSubmitted = false
        score = 0
        quizCompleted = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEng) "Islamic Knowledge Quiz" else "ইসলামিক কুইজ",
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
                        resetQuiz()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextDark)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (!quizCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Progress Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEng) "Question ${currentIndex + 1} of ${questions.size}" else "প্রশ্ন ${currentIndex + 1} / ${questions.size}",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEng) "Score: $score" else "স্কোর: $score",
                            color = PrimaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linear Progress bar
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = PrimaryGreen,
                        trackColor = if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isEng) currentQuestion.questionEng else currentQuestion.questionBn,
                                color = TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options List
                    val options = if (isEng) currentQuestion.optionsEng else currentQuestion.optionsBn
                    options.forEachIndexed { idx, option ->
                        val isSelected = selectedOptionIndex == idx

                        // Determine background and borders on feedback state
                        val cardBgColor = when {
                            answerSubmitted && idx == currentQuestion.correctIndex -> {
                                if (isDarkModeGlobal) Color(0xFF064E3B) else Color(0xFFDCFCE7)
                            }
                            answerSubmitted && isSelected && idx != currentQuestion.correctIndex -> {
                                if (isDarkModeGlobal) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
                            }
                            isSelected -> {
                                PrimaryGreen.copy(alpha = 0.15f)
                            }
                            else -> {
                                CardBg
                            }
                        }

                        val borderColor = when {
                            answerSubmitted && idx == currentQuestion.correctIndex -> {
                                PrimaryGreen
                            }
                            answerSubmitted && isSelected && idx != currentQuestion.correctIndex -> {
                                Color.Red
                            }
                            isSelected -> {
                                PrimaryGreen
                            }
                            else -> {
                                if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)
                            }
                        }

                        val optionTextColor = when {
                            answerSubmitted && idx == currentQuestion.correctIndex -> {
                                if (isDarkModeGlobal) Color(0xFF34D399) else Color(0xFF15803D)
                            }
                            answerSubmitted && isSelected && idx != currentQuestion.correctIndex -> {
                                if (isDarkModeGlobal) Color(0xFFF87171) else Color(0xFFB91C1C)
                            }
                            else -> {
                                TextDark
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !answerSubmitted) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOptionIndex = idx
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            border = BorderStroke(1.2.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic bullet indicator
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isSelected) PrimaryGreen else (if (isDarkModeGlobal) Color(
                                                0xFF334155
                                            ) else Color(0xFFE2E8F0)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (idx + 1).toString(),
                                        color = if (isSelected) Color.White else TextDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = option,
                                    color = optionTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Explanation Box if submitted
                    AnimatedVisibility(
                        visible = answerSubmitted,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkModeGlobal) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ),
                            border = BorderStroke(0.5.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isEng) "Explanation:" else "ব্যাখ্যা:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isEng) currentQuestion.explanationEng else currentQuestion.explanationBn,
                                        fontSize = 11.sp,
                                        color = TextDark,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action Button (Submit or Next)
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (!answerSubmitted) {
                                if (selectedOptionIndex != null) {
                                    answerSubmitted = true
                                    if (selectedOptionIndex == currentQuestion.correctIndex) {
                                        score++
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            } else {
                                if (currentIndex + 1 < questions.size) {
                                    currentIndex++
                                    selectedOptionIndex = null
                                    answerSubmitted = false
                                } else {
                                    quizCompleted = true
                                }
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (!answerSubmitted) {
                                if (isEng) "Submit Answer" else "উত্তর সাবমিট করুন"
                            } else {
                                if (currentIndex + 1 < questions.size) {
                                    if (isEng) "Next Question" else "পরবর্তী প্রশ্ন"
                                } else {
                                    if (isEng) "Show Scorecard" else "ফলাফল দেখুন"
                                }
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Scorecard Overlay screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isEng) "Congratulations!" else "মাশাআল্লাহ! কুইজ সম্পন্ন",
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isEng) "You successfully finished the Islamic Quiz." else "আপনি সফলভাবে কুইজে অংশগ্রহণ সম্পন্ন করেছেন।",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Score Ring
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkModeGlobal) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isEng) "YOUR SCORE" else "আপনার স্কোর",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$score / ${questions.size}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val percentage = (score.toFloat() / questions.size.toFloat() * 100).toInt()
                                val remarks = when {
                                    percentage >= 80 -> if (isEng) "Excellent Knowledge!" else "অসাধারণ ইসলামিক জ্ঞান!"
                                    percentage >= 50 -> if (isEng) "Good Effort!" else "ভালো হয়েছে, চেষ্টা অব্যাহত রাখুন!"
                                    else -> if (isEng) "Keep Learning!" else "ইসলামিক জ্ঞান বৃদ্ধিতে নিয়মিত চর্চা করুন!"
                                }
                                Text(
                                    text = remarks,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Play Again Button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                resetQuiz()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEng) "Play Again" else "পুনরায় খেলুন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Exit button
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }
                        ) {
                            Text(
                                text = if (isEng) "Back to Tools" else "ক্যাটাগরি তালিকায় ফিরে যান",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
