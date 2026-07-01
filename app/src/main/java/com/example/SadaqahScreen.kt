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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class SadaqahLog(
    val id: String,
    val amount: Double,
    val typeEng: String,
    val typeBn: String,
    val dateString: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SadaqahScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish
    val prefs = remember { context.getSharedPreferences("sadaqah_planner_prefs", Context.MODE_PRIVATE) }

    // Load Goal & Logs
    var goalAmount by remember { mutableStateOf(prefs.getFloat("sadaqah_goal", 500f).toDouble()) }
    var logsJsonStr by remember { mutableStateOf(prefs.getString("sadaqah_logs_list", "[]") ?: "[]") }

    val logsList = remember(logsJsonStr) {
        val list = mutableListOf<SadaqahLog>()
        try {
            val arr = JSONArray(logsJsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SadaqahLog(
                        id = obj.getString("id"),
                        amount = obj.getDouble("amount"),
                        typeEng = obj.getString("typeEng"),
                        typeBn = obj.getString("typeBn"),
                        dateString = obj.getString("dateString")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list.sortedByDescending { it.dateString }
    }

    // Calculations
    val totalDonations = logsList.sumOf { it.amount }
    val progressFraction = if (goalAmount > 0) (totalDonations / goalAmount).toFloat().coerceIn(0f, 1f) else 0f

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEng) "Sadaqah Planner" else "সাদাকাহ প্ল্যানার",
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
        containerColor = BgLight,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddDialog = true
                },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Sadaqah")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEng) "Log Sadaqah" else "সাদাকাহ যুক্ত করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Premium Target Goal & Progress Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDarkModeGlobal) {
                                    listOf(Color(0xFF3B0764), Color(0xFF1E1B4B))
                                } else {
                                    listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3))
                                }
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEng) "Monthly Sadaqah Goal" else "মাসিক সাদাকাহ লক্ষ্যমাত্রা",
                                color = TextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isEng) "Goal: ৳${goalAmount.toInt()}" else "লক্ষ্যমাত্রা: ৳${goalAmount.toInt()}",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Customize Goal TextButton
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showGoalDialog = true
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen)
                        ) {
                            Text(
                                text = if (isEng) "Set Goal" else "লক্ষ্য সেট করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Circle or Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEng) "Completed Contributions" else "মোট প্রদান করা হয়েছে",
                                color = TextDark.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "৳${totalDonations.toInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryGreen
                            )
                        }

                        // Percentage badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEng) "Sadaqah extinguishes sin as water extinguishes fire." else "সাদাকাহ গুনাহ নিভিয়ে দেয় যেমন পানি আগুন নিভিয়ে দেয়।",
                            color = TextDark.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Donation History Heading
            Text(
                text = if (isEng) "Sadaqah History" else "সাদাকাহ প্রদানের ইতিহাস",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Donation History List
            if (logsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEng) "No contributions recorded yet" else "এখনো কোনো সাদাকাহ রেকর্ড করা হয়নি",
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isEng) "Tap the + button to log your first Sadaqah" else "নিচের বাটনে ট্যাপ করে আপনার প্রথম সাদাকাহ যুক্ত করুন",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logsList, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = BorderStroke(1.dp, if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE5E7EB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom Category badge indicating donation type
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (if (isEng) log.typeEng else log.typeBn).take(1),
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isEng) log.typeEng else log.typeBn,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = log.dateString,
                                            color = TextGray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+ ৳${log.amount.toInt()}",
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Clear single log item
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val arr = JSONArray(logsJsonStr)
                                            val nextArr = JSONArray()
                                            for (i in 0 until arr.length()) {
                                                val obj = arr.getJSONObject(i)
                                                if (obj.getString("id") != log.id) {
                                                    nextArr.put(obj)
                                                }
                                            }
                                            val updated = nextArr.toString()
                                            logsJsonStr = updated
                                            prefs.edit().putString("sadaqah_logs_list", updated).apply()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Log",
                                            tint = TextGray.copy(alpha = 0.6f),
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

    // Goal Setter Dialog
    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(goalAmount.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val amount = goalInput.toDoubleOrNull() ?: 500.0
                        goalAmount = amount
                        prefs.edit().putFloat("sadaqah_goal", amount.toFloat()).apply()
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(if (isEng) "Set Target" else "লক্ষ্য সেট করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text(if (isEng) "Cancel" else "বাতিল", color = TextGray)
                }
            },
            title = {
                Text(
                    text = if (isEng) "Set Monthly Target" else "মাসিক লক্ষ্য নির্ধারণ",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isEng) "Enter your monthly Sadaqah target amount (৳):" else "আপনার মাসিক সাদাকাহ প্রদানের টার্গেট এমাউন্ট লিখুন (৳):",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0),
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Donation Log Dialog
    if (showAddDialog) {
        var logAmountInput by remember { mutableStateOf("") }
        var selectedTypeEng by remember { mutableStateOf("Cash Donation") }
        var selectedTypeBn by remember { mutableStateOf("নগদ দান") }

        val donationTypes = remember {
            listOf(
                Pair("Cash Donation", "নগদ দান"),
                Pair("Feeding Needy", "খাদ্য সাহায্য"),
                Pair("Clothing Gift", "পোশাক দান"),
                Pair("Medical Help", "চিকিৎসা সাহায্য"),
                Pair("Mosque / Islamic", "মসজিদ/ধর্মীয় দান")
            )
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val amt = logAmountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            val dateString = sdf.format(Date())

                            val newObj = JSONObject().apply {
                                put("id", UUID.randomUUID().toString())
                                put("amount", amt)
                                put("typeEng", selectedTypeEng)
                                put("typeBn", selectedTypeBn)
                                put("dateString", dateString)
                            }

                            val currentArr = try {
                                JSONArray(logsJsonStr)
                            } catch (e: Exception) {
                                JSONArray()
                            }
                            currentArr.put(newObj)
                            val updated = currentArr.toString()

                            logsJsonStr = updated
                            prefs.edit().putString("sadaqah_logs_list", updated).apply()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = logAmountInput.trim().isNotEmpty()
                ) {
                    Text(if (isEng) "Log Sadaqah" else "সাদাকাহ যুক্ত করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isEng) "Cancel" else "বাতিল", color = TextGray)
                }
            },
            title = {
                Text(
                    text = if (isEng) "Log Sadaqah Contribution" else "সাদাকাহ অবদান রেকর্ড",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isEng) "Select donation category:" else "দানের ক্যাটাগরি নির্বাচন করুন:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Type choices Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        donationTypes.forEach { (eng, bn) ->
                            val isSel = selectedTypeEng == eng
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) PrimaryGreen.copy(alpha = 0.12f) else (if (isDarkModeGlobal) Color(
                                            0xFF1E293B
                                        ) else Color(0xFFF1F5F9))
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTypeEng = eng
                                        selectedTypeBn = bn
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTypeEng = eng
                                        selectedTypeBn = bn
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEng) eng else bn,
                                    fontSize = 12.sp,
                                    color = TextDark,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isEng) "Amount (৳):" else "পরিমাণ (৳):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = logAmountInput,
                        onValueChange = { logAmountInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0),
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
