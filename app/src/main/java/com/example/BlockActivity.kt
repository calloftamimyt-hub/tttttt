package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                BlockScreen(
                    onClose = {
                        goHomeAndFinish()
                    }
                )
            }
        }
    }

    private fun goHomeAndFinish() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

data class IslamicQuote(
    val quoteBn: String,
    val quoteEn: String,
    val sourceBn: String,
    val sourceEn: String
)

@Composable
fun BlockScreen(onClose: () -> Unit) {
    val isEng = GlobalLanguage.isEnglish
    var secondsLeft by remember { mutableStateOf(5) }

    val quotes = remember {
        listOf(
            IslamicQuote(
                quoteBn = "“ঈমানদারের একটি গুণ হলো অনর্থক কাজ ও কথা থেকে বিরত থাকা।”",
                quoteEn = "“A sign of a believer's good Islam is leaving that which does not concern him.”",
                sourceBn = "(সুনানে তিরমিযী)",
                sourceEn = "(Sunan al-Tirmidhi)"
            ),
            IslamicQuote(
                quoteBn = "“তোমার যৌবনকে বার্ধক্যের আগে এবং অবসর সময়কে ব্যস্ততার আগে কাজে লাগাও।”",
                quoteEn = "“Take benefit of five before five: your youth before your old age, and your free time before your busy times.”",
                sourceBn = "(আল-হাকিম)",
                sourceEn = "(Al-Hakim)"
            ),
            IslamicQuote(
                quoteBn = "“দুটি নিয়ামত রয়েছে যাতে অধিকাংশ মানুষই ক্ষতিগ্রস্ত: একটি সুস্থতা, অপরটি অবসর সময়।”",
                quoteEn = "“There are two blessings which many people lose: health and free time.”",
                sourceBn = "(সহীহ বুখারী)",
                sourceEn = "(Sahih al-Bukhari)"
            ),
            IslamicQuote(
                quoteBn = "“নিশ্চয়ই কান, চোখ ও অন্তর—এদের প্রত্যেকটি সম্পর্কেই কিয়ামতের দিন কৈফিয়ত তলব করা হবে।”",
                quoteEn = "“Indeed, the hearing, the sight and the heart - about all those you will be questioned.”",
                sourceBn = "(সূরা আল-ইসরা: ৩৬)",
                sourceEn = "(Surah Al-Isra: 36)"
            ),
            IslamicQuote(
                quoteBn = "“লজ্জা ও শালীনতা ঈমানের একটি অতি গুরুত্বপূর্ণ অংশ।”",
                quoteEn = "“Modesty and bashfulness are a critical branch of faith.”",
                sourceBn = "(সহীহ বুখারী)",
                sourceEn = "(Sahih al-Bukhari)"
            )
        )
    }

    val selectedQuote = remember { quotes.random() }

    // Start 5 seconds countdown
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF022C22)  // Emerald 950 (Islamic feeling dark color)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Icon with elegant glowing border
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Blocked",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(40.dp)
                )
            }

            // Screen Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isEng) "Focus Protection Active" else "ফোকাস সুরক্ষা সক্রিয়",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isEng) "This app is locked to protect your time and deen." 
                           else "আপনার সময় ও দীন সুরক্ষার জন্য এই অ্যাপটি সাময়িকভাবে ব্লক করা হয়েছে।",
                    fontSize = 13.5.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            // Beautiful Quote Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isEng) selectedQuote.quoteEn else selectedQuote.quoteBn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF1F5F9),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = if (isEng) selectedQuote.sourceEn else selectedQuote.sourceBn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Timer display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    // Progress sweep
                    CircularProgressIndicator(
                        progress = { secondsLeft / 5f },
                        color = PrimaryGreen,
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxSize()
                    )
                    // Inner Countdown Number
                    Text(
                        text = if (isEng) secondsLeft.toString() else secondsLeft.toBengaliString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Text(
                    text = if (isEng) "Closing in ${secondsLeft}s..." else "${secondsLeft} সেকেন্ডের মধ্যে বন্ধ হয়ে যাবে...",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Immediate Exit button
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isEng) "Okay, I will return" else "ঠিক আছে, আমি ফিরে যাচ্ছি",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Bengali digits helper
private fun Int.toBengaliString(): String {
    val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return this.toString().map { if (it.isDigit()) banglaDigits[it - '0'] else it }.joinToString("")
}
