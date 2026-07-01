package com.example

import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.provider.Settings
import android.os.PowerManager
import android.content.ComponentName
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteBlockerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }
    val isEng = GlobalLanguage.isEnglish

    var isBlocked by remember { mutableStateOf(sharedPrefs.getBoolean("web_blocked", false)) }

    // Custom website blocking list
    var customUrlsString by remember { mutableStateOf(sharedPrefs.getString("custom_blocked_urls", "") ?: "") }
    var urlInput by remember { mutableStateOf("") }

    val urlList = remember(customUrlsString) {
        if (customUrlsString.isEmpty()) emptyList() else customUrlsString.split(",").filter { it.isNotBlank() }
    }

    val updateUrlList = { newList: List<String> ->
        val savedString = newList.joinToString(",")
        customUrlsString = savedString
        sharedPrefs.edit().putString("custom_blocked_urls", savedString).apply()
    }

    // Permission Dialog State
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Helper functions for permission checks
    fun isAccessibilityServiceEnabled(ctx: Context): Boolean {
        val expectedComponentName = ComponentName(ctx, SocialAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }

    fun isBatteryOptimizationIgnored(ctx: Context): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    val toggleBlockFilter = { checked: Boolean ->
        if (checked) {
            if (!isAccessibilityServiceEnabled(context) || !isBatteryOptimizationIgnored(context)) {
                showPermissionDialog = true
            } else {
                isBlocked = true
                sharedPrefs.edit().putBoolean("web_blocked", true).apply()
            }
        } else {
            isBlocked = false
            sharedPrefs.edit().putBoolean("web_blocked", false).apply()
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEng) "Haram Website Blocker" else "হারাম ওয়েবসাইট ব্লকার",
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
            // Shield Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isBlocked) Color(0xFFECFDF5) else Color(0xFFFFFBEB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isBlocked) Color(0xFF10B982) else Color(0xFFF59E0B),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (isBlocked) {
                            if (isEng) "Filter Protection Active" else "সুরক্ষা ফিল্টার সক্রিয় আছে"
                        } else {
                            if (isEng) "Filter Protection Off" else "সুরক্ষা ফিল্টার বন্ধ আছে"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBlocked) Color(0xFF065F46) else Color(0xFF92400E)
                    )
                    Text(
                        text = if (isBlocked) {
                            if (isEng) "✓ Over 1,500 adult/haram websites are currently blocked."
                            else "✓ ১,৫০০টির বেশি হারাম ও পর্নোগ্রাফিক ওয়েবসাইটের লিঙ্ক ব্লক করা হয়েছে আপনার সুরক্ষায়।"
                        } else {
                            if (isEng) "Activate the protection shield to block adult/haram content automatically."
                            else "আপনার ডিভাইসকে পর্নোগ্রাফি ও অশালীন বিষয়বস্তু থেকে মুক্ত রাখতে নিচে ফিল্টারটি সক্রিয় করুন।"
                        },
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        color = if (isBlocked) Color(0xFF047857) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Blocker Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isBlocked) Color(0xFF10B982) else Color(0xFF64748B)
                        )
                        Column {
                            Text(
                                text = if (isEng) "Anti-Porn Haram Filter" else "হারাম এন্টি-পর্ন ব্লক ফিল্টার",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isEng) "Automatic web filter shield" else "স্বয়ংক্রিয় ওয়েব ফিল্টার গার্ড",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                    Switch(
                        checked = isBlocked,
                        onCheckedChange = toggleBlockFilter,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B982)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Custom Blocklist
            Text(
                text = if (isEng) "Custom Website Blocklist" else "কাস্টম ওয়েবসাইট ব্লক তালিকা",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isEng) "Add custom domain/URLs to block manually (e.g. facebook.com):"
                               else "ম্যানুয়ালি ব্লক করার জন্য লিঙ্ক বা ডোমেইন যুক্ত করুন (যেমন: facebook.com):",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text("domain.com", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                val cleanUrl = urlInput.trim().lowercase()
                                if (cleanUrl.isNotEmpty() && !urlList.contains(cleanUrl)) {
                                    val newList = urlList + cleanUrl
                                    updateUrlList(newList)
                                    urlInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Blocked list items
                    if (urlList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEng) "No custom websites blocked yet." else "কোনো কাস্টম ওয়েবসাইট যুক্ত করা হয়নি।",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            urlList.forEach { url ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF1F2), shape = RoundedCornerShape(8.dp))
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = url,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE11D48)
                                    )
                                    IconButton(
                                        onClick = {
                                            val newList = urlList.filter { it != url }
                                            updateUrlList(newList)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Beautiful Permission rounded popup (Matches SocialMediaBlockerScreen flow exactly)
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = if (isEng) "System Permissions Needed" else "সিস্টেমের অনুমতি প্রয়োজন",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (isEng) {
                        "To block websites and protect your focus in real-time, please enable Battery Optimization bypass and our Accessibility Service. This keeps the blocker running stably in the background."
                    } else {
                        "রিয়েল-টাইমে ডিস্ট্রাক্টিং ওয়েবসাইট ব্লক করতে এবং ফোকাস ধরে রাখতে অনুগ্রহ করে ব্যাটারি অপ্টিমাইজেশন বাইপাস এবং আমাদের অ্যাক্সেসিবিলিটি সার্ভিস সক্রিয় করুন। এটি ব্যাকগ্রাউন্ডে ব্লকারটিকে সক্রিয় রাখতে প্রয়োজনীয়।"
                    },
                    fontSize = 13.5.sp,
                    color = TextGray,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            if (!isAccessibilityServiceEnabled(context)) {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } else if (!isBatteryOptimizationIgnored(context)) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error opening settings", Toast.LENGTH_SHORT).show()
                        }

                        // Enable immediately
                        isBlocked = true
                        sharedPrefs.edit().putBoolean("web_blocked", true).apply()
                        showPermissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (isEng) "Open Settings" else "সেটিংস খুলুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = if (isEng) "Cancel" else "বাতিল",
                        color = TextGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}
