package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.ViewState
import com.example.viewmodel.toBengali

@Composable
fun SalatTimesCard(state: ViewState) {
    state.prayerTimes?.let { times ->
        val isEng = GlobalLanguage.isEnglish

        // Formatting Helpers
        val formatTime = { h: Double ->
            val totalSeconds = (h * 3600).toInt()
            val normalizedSeconds = ((totalSeconds % (24 * 3600)) + 24 * 3600) % (24 * 3600)
            val hour = normalizedSeconds / 3600
            val min = (normalizedSeconds / 60) % 60
            val p = if (hour >= 12) {
                if (isEng) "PM" else "পি.এম"
            } else {
                if (isEng) "AM" else "এ.এম"
            }
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val timeStr = String.format("%02d:%02d %s", displayHour, min, p)
            if (isEng) timeStr else timeStr.toBengali()
        }

        // Single Premium Card: Edge-to-Edge, Compact for Social Feed style
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp), // Minimal gap for "connected" feel
            shape = androidx.compose.ui.graphics.RectangleShape, // Edge-to-edge
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Fard Prayers Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF10B982),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEng) "Five Fard Prayers" else "পাঁচ ওয়াক্ত ফরজ নামাজ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fard Prayers List (Slim, Compact, Full Width)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp) // Even tighter spacing
                ) {
                    SalatRowPremium(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Fajr" else "ফজর",
                        time = formatTime(times.fajrHours),
                        isActive = state.currentPrayerName == "Fajr",
                        countdown = state.fajrCountdown
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Dhuhr" else "যোহর",
                        time = formatTime(times.dhuhrHours),
                        isActive = state.currentPrayerName == "Dhuhr",
                        countdown = state.dhuhrCountdown
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.Cloud,
                        name = if (isEng) "Asr" else "আসর",
                        time = formatTime(times.asrHours),
                        isActive = state.currentPrayerName == "Asr",
                        countdown = state.asrCountdown
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Maghrib" else "মাগরিব",
                        time = formatTime(times.maghribHours),
                        isActive = state.currentPrayerName == "Maghrib",
                        countdown = state.maghribCountdown
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.ModeNight,
                        name = if (isEng) "Isha" else "এশা",
                        time = formatTime(times.ishaHours),
                        isActive = state.currentPrayerName == "Isha",
                        countdown = state.ishaCountdown
                    )
                }

                // Decorative Divider
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = Color(0xFFF1F5F9)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Nafil Salat Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEng) "Nafil Salat" else "নফল নামাজ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nafil Salat List (Vertical List)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SalatRowPremium(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Tahajjud" else "তাহাজ্জুদ",
                        time = formatTime(times.fajrHours - 1.2),
                        isActive = false
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.MenuBook,
                        name = if (isEng) "Ishraq" else "ইশরাক",
                        time = formatTime(times.sunriseHours + 0.3),
                        isActive = false
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.WbSunny,
                        name = if (isEng) "Chasht" else "চাশত",
                        time = formatTime(times.sunriseHours + 1.5),
                        isActive = false
                    )
                    SalatRowPremium(
                        icon = Icons.Outlined.WbTwilight,
                        name = if (isEng) "Awwabin" else "আওয়াবীন",
                        time = formatTime(times.maghribHours + 0.3),
                        isActive = false
                    )
                }
            }
        }
    }
}

@Composable
fun ForbiddenTimeCardPremium(
    title: String,
    start: String,
    end: String,
    icon: ImageVector,
    countdown: String = "",
    isEng: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // Soft red background indicating forbidden times
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFEE2E2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color(0xFF1E293B),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = start,
                color = Color(0xFFEF4444),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isEng) "to" else "থেকে",
                color = Color(0xFF64748B),
                fontSize = 9.sp
            )
            Text(
                text = end,
                color = Color(0xFFEF4444),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (countdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = countdown,
                    color = Color(0xFFEF4444),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SalatRowPremium(
    icon: ImageVector,
    name: String,
    time: String,
    isActive: Boolean,
    countdown: String = ""
) {
    val containerBg = if (isActive) Color(0xFF10B982).copy(alpha = 0.08f) else Color(0xFFF8FAFC)
    val nameColor = if (isActive) Color(0xFF10B982) else Color(0xFF334155)
    val timeColor = if (isActive) Color(0xFF10B982) else Color(0xFF0F172A)
    val iconColor = if (isActive) Color(0xFF10B982) else Color(0xFF64748B)
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerBg, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Name and Countdown
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = fontWeight,
                color = nameColor,
                maxLines = 1
            )
            if (countdown.isNotEmpty()) {
                Text(
                    text = countdown,
                    fontSize = 9.sp,
                    color = if (isActive) Color(0xFF10B982) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        // Time Value
        Text(
            text = time,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = timeColor,
            maxLines = 1
        )
    }
}
