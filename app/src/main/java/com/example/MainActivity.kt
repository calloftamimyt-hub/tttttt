package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.toBengali
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.GlobalLanguage
import com.example.receiver.SocialBlockerService
import com.example.receiver.AlarmHelper
import com.example.calculator.PrayerTimes
import com.example.ui.LocalAppStrings
import com.example.ui.theme.*
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.permissions.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import android.net.Uri
import coil.compose.rememberAsyncImagePainter
import android.content.Context
import kotlinx.coroutines.flow.*


private fun getBitmapFromUri(context: android.content.Context, uri: android.net.Uri): android.graphics.Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            
            // Calculate inSampleSize
            var inSampleSize = 1
            val reqWidth = 200
            val reqHeight = 200
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            
            context.contentResolver.openInputStream(uri)?.use { inputStream2 ->
                val finalOptions = android.graphics.BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                android.graphics.BitmapFactory.decodeStream(inputStream2, null, finalOptions)
            }
        }
    } catch (e: Throwable) {
        null
    }
}

class MainActivity : ComponentActivity() {
    var interceptedPlatformName by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.getBooleanExtra("action_intercept_social", false)) {
            interceptedPlatformName = intent.getStringExtra("intercepted_platform_name")
        } else if (intent != null && intent.action == Intent.ACTION_MAIN) {
            // If the user manually opens the app, clear any previous interception state
            interceptedPlatformName = null
        }
    }

    @OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themePrefs = remember(context) { context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE) }
            val isDarkMode by remember(context) {
                val flow = kotlinx.coroutines.flow.MutableStateFlow(themePrefs.getBoolean("dark_mode", false))
                themePrefs.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == "dark_mode") {
                        flow.value = sharedPreferences.getBoolean(key, false)
                    }
                }
                flow
            }.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                val activePlatform = interceptedPlatformName

                // Handle starting/stopping service reactively based on preferences
                val sharedPrefs = remember(context) { context.getSharedPreferences("profile_prefs", Activity.MODE_PRIVATE) }
                
                val settingsViewModel: com.example.viewmodel.SettingsViewModel = viewModel(
                    factory = remember(context) {
                        object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return com.example.viewmodel.SettingsViewModel(context) as T
                            }
                        }
                    }
                )
                val appLanguage by settingsViewModel.language.collectAsState()
                val strings = com.example.ui.getString(appLanguage)
                
                val onboardingPrefs = remember(context) { context.getSharedPreferences("onboarding_prefs", Activity.MODE_PRIVATE) }
                var isOnboardingNeeded by remember { mutableStateOf(!onboardingPrefs.getBoolean("onboarding_completed", false)) }
                var showSplash by remember { mutableStateOf(true) }

                // Reactive state for social blocked status
                val isSocialBlockedState = remember { mutableStateOf(sharedPrefs.getBoolean("social_blocked", false)) }
                
                // Listener to update state when preferences change
                DisposableEffect(sharedPrefs) {
                    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                        if (key == "social_blocked") {
                            isSocialBlockedState.value = prefs.getBoolean("social_blocked", false)
                        }
                    }
                    sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                    onDispose {
                        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }

                // Notification Action Permissions for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                    LaunchedEffect(Unit) {
                        if (notificationPermissionState.status is PermissionStatus.Denied) {
                            notificationPermissionState.launchPermissionRequest()
                        }
                    }
                }
                
                // Exact Alarm check for Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        LaunchedEffect(Unit) {
                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                }

                CompositionLocalProvider(com.example.ui.LocalAppStrings provides strings) {
                    LaunchedEffect(isSocialBlockedState.value, context) {
                        if (isSocialBlockedState.value && SocialBlockerService.isPermissionGranted(context)) {
                            SocialBlockerService.startService(context)
                        } else {
                            SocialBlockerService.stopService(context)
                        }
                    }

                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
                        label = "splash_fade_transition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            if (isOnboardingNeeded) {
                                OnboardingScreen(
                                    prayerViewModel = viewModel() , // We'll get it from ViewModelProvider if needed or just use default here
                                    settingsViewModel = settingsViewModel,
                                    onComplete = {
                                        onboardingPrefs.edit().putBoolean("onboarding_completed", true).apply()
                                        isOnboardingNeeded = false
                                    }
                                )
                            } else if (activePlatform != null) {
                        SocialBlockerOverlay(
                            platformName = activePlatform,
                            onDismissToHome = {
                                interceptedPlatformName = null
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(homeIntent)
                            }
                        )
                    } else {
                        // Real-time Circle Alert Regional Notification Listener
                        LaunchedEffect(context) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val sharedPrefs = context.getSharedPreferences("profile_prefs", Activity.MODE_PRIVATE)
                            val userDistrict = sharedPrefs.getString("location", "All Bangladesh") ?: "All Bangladesh"
                            val trackerDb = com.example.database.TrackerDatabase.getDatabase(context)
                            val notificationDao = trackerDb.notificationDao()
                            
                            db.collection("videos")
                                .whereEqualTo("isCircleAlert", true)
                                .whereEqualTo("status", "APPROVED")
                                .addSnapshotListener { snapshots, e ->
                                    if (e != null) return@addSnapshotListener
                                    if (snapshots != null) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            for (doc in snapshots.documentChanges) {
                                                if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                                    val data = doc.document.data
                                                    val alertLocation = data["location"] as? String ?: "All Bangladesh"
                                                    val title = data["title"] as? String ?: "Alert"
                                                    val category = data["alertCategory"] as? String ?: ""
                                                    val docId = doc.document.id
                                                    
                                                    // Only notify if location matches or it's nationwide, and not already notified
                                                    if ((alertLocation == "All Bangladesh" || alertLocation == userDistrict) && 
                                                        notificationDao.countByRemoteId("alert_$docId") == 0) {
                                                        
                                                        val isEnglish = GlobalLanguage.isEnglish
                                                        val notifTitle = if (isEnglish) "🔴 Real-time Alert: $title" else "🔴 রিয়েল-টাইম অ্যালার্ট: $title"
                                                        val notifBody = if (isEnglish) "Area: $alertLocation | Type: $category" else "এলাকা: $alertLocation | ধরন: $category"
                                                        
                                                        val entity = com.example.database.NotificationEntity(
                                                            title = notifTitle,
                                                            body = notifBody,
                                                            timestamp = System.currentTimeMillis(),
                                                            type = "GENERAL",
                                                            actorName = data["author"] as? String ?: "Halal Circle",
                                                            remoteId = "alert_$docId"
                                                        )
                                                        notificationDao.insertNotification(entity)
                                                        
                                                        // Show push notification
                                                        val ctx = context
                                                        val brandingPrefs = ctx.getSharedPreferences("app_branding", android.content.Context.MODE_PRIVATE)
                                                        val customLogoUriStr = brandingPrefs.getString("app_logo_uri", null)
                                                        val customLogoBitmap = try {
                                                            customLogoUriStr?.let { Uri.parse(it) }?.let { getBitmapFromUri(ctx, it) }
                                                        } catch (e: Throwable) {
                                                            null
                                                        }

                                                        val notifManager = ctx.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                                        val builder = androidx.core.app.NotificationCompat.Builder(ctx, "halal_circle_notifs")
                                                            .setSmallIcon(R.drawable.ic_notification)
                                                            .setContentTitle(notifTitle)
                                                            .setContentText(notifBody)
                                                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                                            .setAutoCancel(true)
                                                        
                                                        if (customLogoBitmap != null) {
                                                            builder.setLargeIcon(customLogoBitmap)
                                                        }
                                                        
                                                        notifManager.notify("alert_$docId".hashCode(), builder.build())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        }

                        val viewModel: PrayerViewModel = viewModel()
                        val state by viewModel.state.collectAsState()
                        val currentCountryCode by settingsViewModel.selectedCountryCode.collectAsState()
                        
                        // Load saved settings (like Madhab) and update when country changes
                        LaunchedEffect(currentCountryCode) {
                            viewModel.loadSettings(context)
                        }

                        // Notification Sync Logic
                        LaunchedEffect(context) {
                            val supabase = Supabase.client
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val trackerDb = com.example.database.TrackerDatabase.getDatabase(context)
                            val notificationDao = trackerDb.notificationDao()

                            supabase.auth.sessionStatus.collect { status ->
                                if (status is SessionStatus.Authenticated) {
                                    val user = status.session.user
                                    if (user != null) {
                                        db.collection("remote_notifications")
                                            .whereEqualTo("toId", user.id)
                                            .addSnapshotListener { snapshots, e ->
                                            if (e != null) return@addSnapshotListener
                                            if (snapshots != null) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    for (doc in snapshots.documentChanges) {
                                                        if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                                            val remoteId = doc.document.id
                                                            if (notificationDao.countByRemoteId(remoteId) == 0) {
                                                                val data = doc.document.data
                                                                val type = data["type"] as? String ?: "GENERAL"
                                                                val isEnglish = GlobalLanguage.isEnglish
                                                                
                                                                var title = data["title"] as? String ?: "Notification"
                                                                var body = data["body"] as? String ?: ""
                                                                
                                                                // Localize specific video status update notifications from Admin
                                                                when (type) {
                                                                    "APPROVE", "APPROVED" -> {
                                                                        title = if (isEnglish) "Video Approved" else "ভিডিও অ্যাপ্রুভ হয়েছে"
                                                                        body = if (isEnglish) "Your video has been approved." else "আপনার ভিডিওটি এডমিন কর্তৃক অ্যাপ্রুভ করা হয়েছে।"
                                                                    }
                                                                    "REJECT", "REJECTED" -> {
                                                                        title = if (isEnglish) "Video Rejected" else "ভিডিও রিজেক্ট হয়েছে"
                                                                        body = if (isEnglish) "Your video submission was not approved." else "আপনার ভিডিওটি এডমিন কর্তৃক রিজেক্ট করা হয়েছে।"
                                                                    }
                                                                    "DELETE", "DELETED" -> {
                                                                        title = if (isEnglish) "Video Deleted" else "ভিডিও মুছে ফেলা হয়েছে"
                                                                        body = if (isEnglish) "A video was deleted by the admin." else "আপনার ভিডিওটি এডমিন কর্তৃক ডিলেট করা হয়েছে।"
                                                                    }
                                                                }

                                                                val entity = com.example.database.NotificationEntity(
                                                                    title = title,
                                                                    body = body,
                                                                    timestamp = data["timestamp"] as? Long ?: System.currentTimeMillis(),
                                                                    type = type,
                                                                    actorName = data["actorName"] as? String ?: "System",
                                                                    remoteId = remoteId
                                                                )
                                                                notificationDao.insertNotification(entity)
                                                                
                                                                // Show local push notification
                                                                val ctx = context
                                                                val brandingPrefs = ctx.getSharedPreferences("app_branding", android.content.Context.MODE_PRIVATE)
                                                                val customLogoUriStr = brandingPrefs.getString("app_logo_uri", null)
                                                                val customLogoBitmap = try {
                                                                    customLogoUriStr?.let { Uri.parse(it) }?.let { getBitmapFromUri(ctx, it) }
                                                                } catch (e: Throwable) {
                                                                    null
                                                                }

                                                                val notifManager = ctx.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                                    val channel = android.app.NotificationChannel("halal_circle_notifs", "General Notifications", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                                                                    notifManager.createNotificationChannel(channel)
                                                                }
                                                                val notifyIntent = Intent(ctx, MainActivity::class.java).apply {
                                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                    putExtra("open_notifications", true)
                                                                }
                                                                val notifyPendingIntent = android.app.PendingIntent.getActivity(
                                                                    ctx, 0, notifyIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                                                )
                                                                val builder = androidx.core.app.NotificationCompat.Builder(ctx, "halal_circle_notifs")
                                                                    .setSmallIcon(R.drawable.ic_notification)
                                                                    .setContentTitle(entity.title)
                                                                    .setContentText(entity.body)
                                                                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                                                                    .setAutoCancel(true)
                                                                    .setContentIntent(notifyPendingIntent)
                                                                
                                                                if (customLogoBitmap != null) {
                                                                    builder.setLargeIcon(customLogoBitmap)
                                                                }

                                                                notifManager.notify(remoteId.hashCode(), builder.build())
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

                    val permissions = remember {
                            val list = mutableListOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                list.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            // Access to set exact alarms is usually handled separately via Intent if not granted
                            list
                        }

                        LaunchedEffect(Unit) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                }
                            }
                        }

                        val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
                        var selectedTab by remember { mutableStateOf("home") }
                        
                        var isAlarmPageOpen by remember { mutableStateOf(false) }
                        var isZakatPageOpen by remember { mutableStateOf(false) }
                        var isCalendarPageOpen by remember { mutableStateOf(false) }
                        var isQiblaPageOpen by remember { mutableStateOf(false) }
                        var isNotificationsPageOpen by remember { mutableStateOf(false) }
                        var isAddAlarmPageOpen by remember { mutableStateOf(false) }
                        var isParentalPageOpen by remember { mutableStateOf(false) }
                        var isPrayerPageOpen by remember { mutableStateOf(false) }
                        var isCreateCircleAlertOpen by remember { mutableStateOf(false) }
                        var isFoundationPageOpen by remember { mutableStateOf(false) }
                        var isProfileSubScreenOpen by remember { mutableStateOf(false) }
                        
                        val alarmViewModel: com.example.viewmodel.AlarmViewModel = remember { 
                            com.example.viewmodel.AlarmViewModel(context) 
                        }
                        val userAlarms by alarmViewModel.alarms.collectAsState()

                        val activity = LocalContext.current as? androidx.activity.ComponentActivity
                        LaunchedEffect(activity?.intent) {
                            if (activity?.intent?.getBooleanExtra("open_notifications", false) == true) {
                                isNotificationsPageOpen = true
                                activity?.intent?.removeExtra("open_notifications")
                            }
                        }
                        DisposableEffect(activity) {
                            val listener = androidx.core.util.Consumer<Intent> { newIntent ->
                                if (newIntent.getBooleanExtra("open_notifications", false)) {
                                    isNotificationsPageOpen = true
                                    newIntent.removeExtra("open_notifications")
                                }
                            }
                            activity?.addOnNewIntentListener(listener)
                            onDispose {
                                activity?.removeOnNewIntentListener(listener)
                            }
                        }

                        val view = LocalView.current
                        val isProfileOverlayOpen = isAlarmPageOpen || isZakatPageOpen || isCalendarPageOpen || isQiblaPageOpen || isNotificationsPageOpen || isAddAlarmPageOpen || isParentalPageOpen || isPrayerPageOpen || isCreateCircleAlertOpen || isFoundationPageOpen
                        val showBottomBar = !isProfileOverlayOpen && !isProfileSubScreenOpen
                        val isDarkStatusBar = false
                        val isAuthPage = false
                        
                        LaunchedEffect(isDarkMode, isDarkStatusBar, isProfileOverlayOpen, isAuthPage, view) {
                            val window = (view.context as Activity).window
                            window.statusBarColor = android.graphics.Color.TRANSPARENT
                            window.navigationBarColor = if (isDarkMode) android.graphics.Color.parseColor("#1E293B") else android.graphics.Color.WHITE
                            // If profile/overlay is open, or on Auth page, we want dark icons on white/transparent status bar, except when in dark mode
                            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
                            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkMode
                        }

                        LaunchedEffect(multiplePermissionsState.allPermissionsGranted, context) {
                            if (multiplePermissionsState.allPermissionsGranted) {
                                viewModel.startLocationUpdates(context)
                            } else {
                                viewModel.setPermissionDenied()
                            }
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = if (isProfileOverlayOpen) CardBg else (if (isDarkStatusBar) Color.Black else BgLight),
                            bottomBar = { 
                                if (showBottomBar) {
                                    AppBottomNavigation(selectedTab, isDark = isDarkStatusBar) { 
                                        selectedTab = it 
                                        if (isProfileOverlayOpen) {
                                            isAlarmPageOpen = false
                                            isZakatPageOpen = false
                                            isCalendarPageOpen = false
                                            isQiblaPageOpen = false
                                            isNotificationsPageOpen = false
                                            isAddAlarmPageOpen = false
                                            isParentalPageOpen = false
                                            isPrayerPageOpen = false
                                            isCreateCircleAlertOpen = false
                                        }
                                    } 
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isProfileOverlayOpen) {
                                            Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                        } else {
                                            Modifier.padding(innerPadding)
                                        }
                                    )
                            ) {
                                LaunchedEffect(Unit) {
                                    if (!multiplePermissionsState.allPermissionsGranted && state.isAutoLocation) {
                                        multiplePermissionsState.launchMultiplePermissionRequest()
                                    }
                                }

                                if (selectedTab == "home") {
                                        HomeScreen(
                                            state = state,
                                            onToggleAlarm = { viewModel.toggleAlarm(context, it) },
                                            onNavigateToPrayerDetails = { isPrayerPageOpen = true },
                                            onNavigateToTracker = { selectedTab = "tracker" },
                                            onNavigateToTasbih = { selectedTab = "tasbih" },
                                            onNavigateToComingSoon = { selectedTab = "coming_soon" },
                                            onNavigateToQuran = { selectedTab = "quran" },
                                            onNavigateToLocation = { selectedTab = "location" },
                                            onOpenAlarmPage = { isAlarmPageOpen = true },
                                            onNavigateToZakat = { isZakatPageOpen = true },
                                            onNavigateToCalendar = { isCalendarPageOpen = true },
                                            onNavigateToQibla = { isQiblaPageOpen = true },
                                            onNavigateToTools = { selectedTab = "tools" },
                                            onNavigateToAllahNames = { selectedTab = "allah_names" },
                                            onNavigateToRamadan = { selectedTab = "ramadan" },
                                            onNavigateToDua = { selectedTab = "dua" },
                                            onNavigateToHadith = { selectedTab = "hadith" },
                                            onOpenNotificationsPage = { isNotificationsPageOpen = true },
                                            onOpenFoundationPage = { isFoundationPageOpen = true },
                                            onRefreshLocation = { viewModel.forceRefreshLocation(context) }
                                        )
                                    } else if (selectedTab == "location") {
                                        LocationSelectionScreen(
                                            viewModel = viewModel,
                                            onBack = { selectedTab = "home" }
                                        )
                                    } else if (selectedTab == "quran") {
                                        QuranScreen(onBack = { selectedTab = "home" })
                                    } else if (selectedTab == "dua") {
                                        DuaScreen(onBack = { selectedTab = "home" })
                                    } else if (selectedTab == "hadith") {
                                        HadithScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "tracker") {
                                        TrackerScreen(onBack = { selectedTab = "home" })
                                    } else if (selectedTab == "tools") {
                                        ToolsScreen(
                                            onNavigateToTracker = { selectedTab = "tracker" },
                                            onNavigateToTasbih = { selectedTab = "tasbih" },
                                            onNavigateToComingSoon = { selectedTab = "coming_soon" },
                                            onNavigateToQuran = { selectedTab = "quran" },
                                            onNavigateToZakat = { isZakatPageOpen = true },
                                            onNavigateToCalendar = { isCalendarPageOpen = true },
                                            onNavigateToQibla = { isQiblaPageOpen = true },
                                            onNavigateToAllahNames = { selectedTab = "allah_names" },
                                            onNavigateToRamadan = { selectedTab = "ramadan" },
                                            onNavigateToDuroodReminder = { selectedTab = "durood_reminder" },
                                            onNavigateToDua = { selectedTab = "dua" },
                                            onNavigateToHadith = { selectedTab = "hadith" },
                                            onNavigateToWidgets = { selectedTab = "widgets" },
                                            onNavigateToIslamicNames = { selectedTab = "islamic_names" }
                                        )
                                    } else if (selectedTab == "tasbih") {
                                        TasbihScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "widgets") {
                                        HomeScreenWidgetsScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "durood_reminder") {
                                        DuroodReminderScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "islamic_names") {
                                        IslamicNamesScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "ramadan") {
                                        RamadanScreen(state = state, onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "allah_names") {
                                        NamesOfAllahScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "profile") {
                                        ProfileScreen(
                                            onNavigateToTracker = { selectedTab = "tracker" },
                                            onNavigateToSettings = { selectedTab = "settings" },
                                            onNavigateToParentalControl = { isParentalPageOpen = true },
                                            onNavigateToSavedDuas = { selectedTab = "saved_duas" },
                                            onNavigateToSavedAyahs = { selectedTab = "saved_ayahs" },
                                            onNavigateToSavedHadiths = { selectedTab = "saved_hadiths" },
                                            onToggleBottomBar = { isProfileSubScreenOpen = !it }
                                        )
                                    } else if (selectedTab == "saved_duas") {
                                        SavedDuasScreen(onBack = { selectedTab = "profile" })
                                    } else if (selectedTab == "saved_ayahs") {
                                        SavedAyahsScreen(onBack = { selectedTab = "profile" })
                                    } else if (selectedTab == "saved_hadiths") {
                                        SavedHadithsScreen(onBack = { selectedTab = "profile" })
                                    } else if (selectedTab == "coming_soon") {
                                        ComingSoonScreen(onBack = { selectedTab = "tools" })
                                    } else if (selectedTab == "settings") {
                                        SettingsScreen(
                                            viewModel = settingsViewModel,
                                            prayerAlarms = state.alarms,
                                            onTogglePrayerAlarm = { alarmId -> viewModel.toggleAlarm(context, alarmId) },
                                            isAutoLocation = state.isAutoLocation,
                                            onToggleAutoLocation = { enabled -> viewModel.setAutoLocationEnabled(context, enabled) },
                                            onBack = { selectedTab = "profile" }
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("শীঘ্রই আসছে...", fontSize = 24.sp, color = TextGray)
                                        }
                                    }
                                
                                // Overlay status bar header purely for visual gradient, without padding the content below
                                if (!isDarkStatusBar) {
                                    GlassStatusBarHeader()
                                }

                                if (isParentalPageOpen) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isParentalPageOpen,
                                        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
                                    ) {
                                        ParentalControlScreen(
                                            onBack = { isParentalPageOpen = false }
                                        )
                                    }
                                }

                                if (isAlarmPageOpen) {
                                    AlarmSetupScreen(
                                        onBack = { isAlarmPageOpen = false },
                                        onAddAlarmClick = { isAddAlarmPageOpen = true },
                                        alarms = userAlarms,
                                        onToggleAlarm = { alarmViewModel.toggleAlarm(it) },
                                        onDeleteAlarm = { alarmViewModel.deleteAlarm(it) }
                                    )
                                }
                                if (isAddAlarmPageOpen) {
                                    AddAlarmScreen(
                                        onBack = { isAddAlarmPageOpen = false },
                                        onSave = { alarmViewModel.addAlarm(it) }
                                    )
                                }
                                if (isZakatPageOpen) {
                                    ZakatCalculatorScreen(
                                        onBack = { isZakatPageOpen = false }
                                    )
                                }
                                if (isCalendarPageOpen) {
                                    CalendarScreen(
                                        onBack = { isCalendarPageOpen = false }
                                    )
                                }
                                if (isNotificationsPageOpen) {
                                     NotificationsScreen(
                                         onBack = { isNotificationsPageOpen = false }
                                     )
                                 }
                                 if (isFoundationPageOpen) {
                                     FoundationScreen(
                                         onBack = { isFoundationPageOpen = false }
                                     )
                                 }
                                 if (isQiblaPageOpen) {
                                    QiblaCompassScreen(
                                        onBack = { isQiblaPageOpen = false }
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isPrayerPageOpen,
                                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(400)) + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(400)),
                                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(400)) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(400))
                                ) {
                                    PrayerScreen(
                                        state = state,
                                        onBack = { isPrayerPageOpen = false },
                                        onToggleAlarm = { alarmId -> viewModel.toggleAlarm(context, alarmId) },
                                        onOpenAlarmPage = { isAlarmPageOpen = true }
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isCreateCircleAlertOpen,
                                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(400)) + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(400)),
                                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(400)) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(400))
                                ) {
                                    CreateCircleAlertScreen(
                                        savedLocation = state.locationName.ifEmpty { "All Bangladesh" }, // Get location from state
                                        onBack = { isCreateCircleAlertOpen = false },
                                        onSubmit = { alert ->
                                               // Optional: submit to server
                                        }
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

@Composable
fun GlassStatusBarHeader() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
    )
}

@Composable
fun AppBottomNavigation(selectedTab: String, isDark: Boolean, onTabSelected: (String) -> Unit) {
    val navContainerColor = CardBg
    val navUnselectedColor = TextGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
    ) {
        NavigationBar(
            containerColor = navContainerColor,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .navigationBarsPadding()
        ) {
        NavigationBarItem(
            selected = selectedTab == "home",
            onClick = { onTabSelected("home") },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (selectedTab == "home") Modifier.border(2.dp, PrimaryGreen, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == "home") Icons.Default.Home else Icons.Outlined.Home, 
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    ) 
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = navUnselectedColor,
                unselectedTextColor = navUnselectedColor
            )
        )
        NavigationBarItem(
            selected = selectedTab == "quran",
            onClick = { onTabSelected("quran") },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (selectedTab == "quran") Modifier.border(2.dp, PrimaryGreen, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == "quran") Icons.Filled.MenuBook else Icons.Outlined.MenuBook, 
                        contentDescription = "Quran",
                        modifier = Modifier.size(24.dp)
                    ) 
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = navUnselectedColor,
                unselectedTextColor = navUnselectedColor
            )
        )
        NavigationBarItem(
            selected = selectedTab == "tracker",
            onClick = { onTabSelected("tracker") },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (selectedTab == "tracker") Modifier.border(2.dp, PrimaryGreen, RoundedCornerShape(10.dp)) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == "tracker") Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle, 
                        contentDescription = "Tracker",
                        modifier = Modifier.size(24.dp)
                    ) 
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = navUnselectedColor,
                unselectedTextColor = navUnselectedColor
            )
        )
        NavigationBarItem(
            selected = selectedTab == "tools",
            onClick = { onTabSelected("tools") },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (selectedTab == "tools") Modifier.border(2.dp, PrimaryGreen, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == "tools") Icons.Filled.GridView else Icons.Outlined.GridView, 
                        contentDescription = "Tools",
                        modifier = Modifier.size(24.dp)
                    ) 
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = navUnselectedColor,
                unselectedTextColor = navUnselectedColor
            )
        )
        NavigationBarItem(
            selected = selectedTab == "profile",
            onClick = { onTabSelected("profile") },
            icon = { 
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (selectedTab == "profile") Modifier.border(2.dp, PrimaryGreen, CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == "profile") Icons.Filled.Person else Icons.Outlined.Person, 
                        contentDescription = "Profile",
                        modifier = Modifier.size(24.dp)
                    ) 
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = navUnselectedColor,
                unselectedTextColor = navUnselectedColor
            )
        )
    }
}
}

@Composable
fun UnifiedHeroCard(
    state: com.example.viewmodel.ViewState, 
    onNavigateToPrayerDetails: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    val prayerTimes = state.prayerTimes ?: return
    val currentHour = state.currentHourDecimal
    val sunrise = prayerTimes.sunriseHours
    val sunset = prayerTimes.maghribHours
    
    val isDay = currentHour in sunrise..sunset
    
    val progress = if (isDay) {
        ((currentHour - sunrise) / (sunset - sunrise)).coerceIn(0.0, 1.0).toFloat()
    } else {
        // Night progress
        if (currentHour > sunset) {
            ((currentHour - sunset) / (24.0 - sunset + sunrise)).coerceIn(0.0, 1.0).toFloat()
        } else {
            ((currentHour + (24.0 - sunset)) / (24.0 - sunset + sunrise)).coerceIn(0.0, 1.0).toFloat()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onNavigateToPrayerDetails() },
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(CardBg)
                .padding(16.dp)
        ) {
            // Salat Countdown Circle (Positioned in the Center)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.Center).size(130.dp)) {
                // Background circle
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = BgLight)
                }
                CircularProgressIndicator(
                    progress = { state.timerProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryGreen,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = BgLight
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayNames = if (state.rotatingNames.isNotEmpty()) state.rotatingNames else listOf(state.currentPrayerNameBen.ifEmpty { state.nextPrayerNameBen })
                    var currentIndex by remember(displayNames) { mutableIntStateOf(0) }
                    LaunchedEffect(displayNames) {
                        if (displayNames.size > 1) {
                            while(true) {
                                kotlinx.coroutines.delay(3000)
                                currentIndex = (currentIndex + 1) % displayNames.size
                            }
                        } else {
                            currentIndex = 0
                        }
                    }
                    val currentTitle = if (displayNames.isNotEmpty()) displayNames[currentIndex % displayNames.size] else "..."
                    
                    AnimatedContent(
                        targetState = currentTitle,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(600)) + slideInVertically { height -> height }).togetherWith(
                                fadeOut(animationSpec = tween(600)) + slideOutVertically { height -> -height }
                            )
                        },
                        label = "hero_title_rotation"
                    ) { title ->
                        Text(title, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    
                    Text(state.nextPrayerRemaining, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top=2.dp))
                    Text(if (GlobalLanguage.isEnglish) "Time remaining" else "শেষ হতে বাকি", color = TextGray, fontSize = 10.sp, modifier = Modifier.padding(top=2.dp))
                }
            }
            
            // Labels for Sunrise and Sunset at bottom (Sunrise on left, Sunset on right)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbTwilight, 
                        contentDescription = "Sunrise", 
                        tint = Color(0xFFF59E0B), 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (GlobalLanguage.isEnglish) "Sunrise" else "সূর্যোদয়",
                        color = TextDark,
                        fontSize = 10.sp
                    )
                    Text(
                        text = prayerTimes.sunrise.toBengali(),
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny, 
                        contentDescription = "Sunset", 
                        tint = Color(0xFFEA580C), 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (GlobalLanguage.isEnglish) "Sunset" else "সূর্যাস্ত",
                        color = TextDark,
                        fontSize = 10.sp
                    )
                    Text(
                        text = prayerTimes.maghrib.toBengali(),
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}



@Composable
fun HomeScreen(
    state: com.example.viewmodel.ViewState, 
    onToggleAlarm: (String) -> Unit, 
    onNavigateToPrayerDetails: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToComingSoon: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onOpenAlarmPage: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToAllahNames: () -> Unit,
    onNavigateToRamadan: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onOpenNotificationsPage: () -> Unit,
    onOpenFoundationPage: () -> Unit,
    onRefreshLocation: () -> Unit = {}
) {
    var isPrayerExpanded by remember { mutableStateOf(false) }
    
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight) // Adaptive "feed" background
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar (Top row: Clickable App Name with Quick Action Icons aligned perfectly on the right)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
            // Header Title and Location
            val context = LocalContext.current
            val brandingPrefs = remember { context.getSharedPreferences("app_branding", Context.MODE_PRIVATE) }
            val customLogoUriStr = brandingPrefs.getString("app_logo_uri", null)
            val customLogoUri = remember(customLogoUriStr) { customLogoUriStr?.let { Uri.parse(it) } }

            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Halal Circle",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PrimaryGreen
                    )
                }
                
                // Location Badge: styled beautifully with a light green background/border
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(PrimaryGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { onNavigateToLocation() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = state.locationName,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen,
                            fontSize = 11.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Location",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenAlarmPage) {
                        Icon(
                            imageVector = Icons.Outlined.AccessAlarm, 
                            contentDescription = "Alarms", 
                            tint = TextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    val themePrefs = remember { context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE) }
                    IconButton(
                        onClick = {
                            val currentMode = themePrefs.getBoolean("dark_mode", false)
                            themePrefs.edit().putBoolean("dark_mode", !currentMode).apply()
                        }
                    ) {
                        Icon(
                            imageVector = if (isDarkModeGlobal) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkModeGlobal) Color(0xFFFFD700) else TextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(onClick = onOpenFoundationPage) {
                        Icon(
                            imageVector = Icons.Outlined.VolunteerActivism, 
                            contentDescription = "Foundation", 
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Box {
                        val context = LocalContext.current
                        val unreadCount by remember {
                            com.example.database.TrackerDatabase.getDatabase(context).notificationDao().getUnreadCount()
                        }.collectAsState(initial = 0)
    
                        IconButton(
                            onClick = onOpenNotificationsPage
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications, 
                                contentDescription = "Notifications", 
                                tint = TextDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        if (unreadCount > 0) {
                            val displayCount = if (unreadCount > 99) (if (GlobalLanguage.isEnglish) "99+" else "৯৯+") else if (GlobalLanguage.isEnglish) unreadCount.toString() else unreadCount.toBengaliDigits()
                            val badgeSize = if (unreadCount > 9) 20.dp else 18.dp
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                                    .defaultMinSize(minWidth = badgeSize, minHeight = badgeSize)
                                    .background(Color.Red, CircleShape)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayCount,
                                    color = Color.White,
                                    fontSize = if (unreadCount > 9) 9.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Dates Below Icons
                val cal = java.util.Calendar.getInstance()
                val year = cal.get(java.util.Calendar.YEAR)
                val month = cal.get(java.util.Calendar.MONTH)
                val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
                val isEng = GlobalLanguage.isEnglish
                
                val gregDayStr = if (isEng) day.toString() else com.example.HijriCalendarHelper.toBengaliNumber(day)
                val gregMonthStr = if (isEng) com.example.HijriCalendarHelper.enGregorianMonths[month] else com.example.HijriCalendarHelper.bnGregorianMonths[month]
                val gregYearStr = if (isEng) year.toString() else com.example.HijriCalendarHelper.toBengaliNumber(year)
                val gregorianDate = "$gregDayStr $gregMonthStr $gregYearStr"
                
                val hDate = com.example.HijriCalendarHelper.gregorianToHijri(year, month + 1, day)
                val hijriDayStr = if(isEng) hDate.day.toString() else com.example.HijriCalendarHelper.toBengaliNumber(hDate.day)
                val hijriMonthStr = if(isEng) com.example.HijriCalendarHelper.enHijriMonths[hDate.month - 1] else com.example.HijriCalendarHelper.bnHijriMonths[hDate.month - 1]
                val hijriYearStr = if(isEng) hDate.year.toString() else com.example.HijriCalendarHelper.toBengaliNumber(hDate.year)
                val hijriDate = if(isEng) "$hijriDayStr $hijriMonthStr, $hijriYearStr AH" else "$hijriDayStr $hijriMonthStr, $hijriYearStr হিজরি"

                Text(
                    text = hijriDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = gregorianDate,
                    fontSize = 11.sp,
                    color = TextGray,
                    modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
                )
            }
        }
    }

    // Unified Hero Section (Sun/Moon Arc + Countdown)
    UnifiedHeroCard(state, onNavigateToPrayerDetails, onNavigateToLocation)

    // Sub info (Sehri / Iftar & Live Countdown) - Slim Feed Card
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        color = CardBg,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val isEng = GlobalLanguage.isEnglish
            
            // Column 1: Sehri Ends Time
            val sehriLastTime = state.prayerTimes?.fajr ?: "--:--"
            val sehriLastTimeDisp = if (isEng) sehriLastTime else sehriLastTime.toBengali()
            val sehriLabel = if (isEng) "Sehri Ends" else "পরবর্তী সাহরি"
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sehriLastTimeDisp,
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sehriLabel,
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(35.dp)
                    .background(if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
            )
            
            // Column 2: Iftar Starts Time
            val iftarTime = state.prayerTimes?.maghrib ?: "--:--"
            val iftarTimeDisp = if (isEng) iftarTime else iftarTime.toBengali()
            val iftarLabel = if (isEng) "Iftar Starts" else "পরবর্তী ইফতার"
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = iftarTimeDisp,
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = iftarLabel,
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(35.dp)
                    .background(if (isDarkModeGlobal) Color(0xFF334155) else Color(0xFFE2E8F0))
            )
            
            // Column 3: Live Countdown
            val countdownTime = state.specialCountdownTime
            val countdownLabel = state.specialCountdownLabel
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = countdownTime,
                    color = PrimaryGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = countdownLabel,
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Salat Times Section (Nafal & Farz)
    SalatTimesCard(state)

    Spacer(modifier = Modifier.height(4.dp))

    // Forbidden Prayer Times Section (Full Width Feed Style)
    val isEng = GlobalLanguage.isEnglish
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DoNotDisturbAlt,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isEng) "Forbidden Prayer Times" else "নিষিদ্ধ সালাতের সময়",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vertical List (Clean & Premium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                @Composable
                fun ForbiddenTimeRow(title: String, time: String, countdown: String) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkModeGlobal) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFFFF1F2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (countdown.isNotEmpty()) {
                                Text(
                                    text = countdown,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = time,
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                ForbiddenTimeRow(
                    title = if (isEng) "Sunrise" else "সূর্যোদয়",
                    time = "${state.forbiddenSunrise} - ${state.forbiddenSunriseEnd}",
                    countdown = state.forbiddenSunriseCountdown
                )
                ForbiddenTimeRow(
                    title = if (isEng) "Noon" else "মধ্যাহ্ন",
                    time = "${state.forbiddenNoon} - ${state.forbiddenNoonEnd}",
                    countdown = state.forbiddenNoonCountdown
                )
                ForbiddenTimeRow(
                    title = if (isEng) "Sunset" else "সূর্যাস্ত",
                    time = "${state.forbiddenSunset} - ${state.forbiddenSunsetEnd}",
                    countdown = state.forbiddenSunsetCountdown
                )
            }
        }
    }
    }
}



@Composable
fun SubInfoItem(title: String, time: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(title, color = TextGray, fontSize = 12.sp, fontWeight=FontWeight.Medium)
        Spacer(modifier=Modifier.height(2.dp))
        Text(time, color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SubInfoItemProgress(
    title: String, 
    time: String, 
    progressFloat: Float, 
    progressText: String, 
    isLarge: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isLarge) 60.dp else 44.dp)) {
            CircularProgressIndicator(
                progress = { 1f },
                strokeWidth = if (isLarge) 4.dp else 3.dp,
                color = BgLight,
                modifier = Modifier.fillMaxSize()
            )
            CircularProgressIndicator(
                progress = { progressFloat },
                strokeWidth = if (isLarge) 4.dp else 3.dp,
                color = PrimaryGreen,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = progressText, 
                fontSize = if (isLarge) 14.sp else 10.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Black
            )
        }
        Column {
            Text(title, fontSize = if (isLarge) 12.sp else 9.sp, color = TextGray)
            Text(time, fontSize = if (isLarge) 22.sp else 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun PrayerRow(
    name: String, 
    startTime: String, 
    endTime: String,
    isAlarmOn: Boolean, 
    isActive: Boolean, 
    onToggleAlarm: () -> Unit,
    onOpenAlarmPage: () -> Unit
) {
    val cleanStart = startTime.replace(" এএম", "").replace(" পিএম", "").replace(" AM", "").replace(" PM", "").toBengali()
    val cleanEnd = endTime.replace(" এএম", "").replace(" পিএম", "").replace(" AM", "").replace(" PM", "").toBengali()
    val endSuffix = if (endTime.contains("পিএম") || endTime.contains("PM")) (if(GlobalLanguage.isEnglish) " PM" else " পিএম") else (if(GlobalLanguage.isEnglish) " AM" else " এএম")
    
    // For night time range like Isha to Fajr
    val timeDisplay = if (name == "এশা" || name == "Isha") {
        "$cleanStart - ${if (GlobalLanguage.isEnglish) "Night" else "রাত"} $cleanEnd".toBengali()
    } else {
        "$cleanStart - $cleanEnd$endSuffix".toBengali()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if(isActive) PrimaryGreen.copy(alpha=0.12f) else Color.Transparent, 
                RoundedCornerShape(12.dp)
            )
            .then(
                if(isActive) Modifier.border(1.dp, PrimaryGreen.copy(alpha=0.3f), RoundedCornerShape(12.dp)) else Modifier
            )
            .clickable { onOpenAlarmPage() }
            .padding(horizontal=12.dp, vertical=8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(if(isActive) PrimaryGreen else BgLight.copy(alpha=0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (name == "এশা" || name == "মাগরিব" || name=="ফজর" || name == "Isha" || name == "Maghrib" || name == "Fajr") 
                        Icons.Outlined.DarkMode else Icons.Outlined.LightMode, 
                    contentDescription = null, 
                    tint = if(isActive) Color.White else TextGray, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                name, 
                color = TextDark, 
                fontSize = 17.sp, 
                fontWeight=if(isActive) FontWeight.ExtraBold else FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                timeDisplay, 
                color = PrimaryGreen, 
                fontSize = 16.sp, 
                fontWeight=if(isActive) FontWeight.ExtraBold else FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onToggleAlarm, modifier = Modifier.size(24.dp)) {
                 Icon(
                     if (isAlarmOn) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                     contentDescription = "Alarm",
                     tint = if (isAlarmOn) PrimaryGreen else TextGray.copy(alpha = 0.6f),
                     modifier = Modifier.size(20.dp)
                 )
            }
        }
    }
}

data class Quad<A, B, C, D>(val id: A, val name: B, val startTime: C, val endTime: D)

@Composable
fun CategoryScrollableRow(
    onNavigateToTracker: () -> Unit,
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToComingSoon: () -> Unit = {},
    onNavigateToQuran: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToAllahNames: () -> Unit = {},
    onNavigateToRamadan: () -> Unit = {},
    onNavigateToDua: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToIslamicNames: () -> Unit = {}
) {
    val items = if (GlobalLanguage.isEnglish) {
        listOf(
            Triple("Al Quran", Icons.Outlined.MenuBook, PrimaryGreen),
            Triple("Hadith", Icons.Outlined.LibraryBooks, Color(0xFF3B82F6)),
            Triple("Tasbih", Icons.Outlined.Album, Color(0xFF8B5CF6)),
            Triple("Qibla", Icons.Outlined.Explore, PrimaryGreen),
            Triple("Dua", Icons.Outlined.WavingHand, Color(0xFFEC4899)),
            Triple("Allah's Names", Icons.Outlined.Star, Color(0xFFF59E0B)),
            Triple("Zakat", Icons.Outlined.MonetizationOn, PrimaryGreen),
            Triple("Calendar", Icons.Outlined.CalendarMonth, Color(0xFF6366F1)),
            Triple("Amal Learning", Icons.Outlined.School, Color(0xFF8B5CF6)),
            Triple("Ramadan", Icons.Outlined.ModeNight, Color(0xFF6366F1)),
            Triple("Islamic Name", Icons.Outlined.People, Color(0xFF3B82F6)),
            Triple("Salah Learning", Icons.Outlined.SelfImprovement, Color(0xFF14B8A6))
        )
    } else {
        listOf(
            Triple("আল কুরআন", Icons.Outlined.MenuBook, PrimaryGreen),
            Triple("হাদিস", Icons.Outlined.LibraryBooks, Color(0xFF3B82F6)),
            Triple("তাসবিহ", Icons.Outlined.Album, Color(0xFF8B5CF6)),
            Triple("কিবলা", Icons.Outlined.Explore, PrimaryGreen),
            Triple("দোয়া", Icons.Outlined.WavingHand, Color(0xFFEC4899)),
            Triple("আল্লাহর নাম", Icons.Outlined.Star, Color(0xFFF59E0B)),
            Triple("যাকাত", Icons.Outlined.MonetizationOn, PrimaryGreen),
            Triple("ক্যালেন্ডার", Icons.Outlined.CalendarMonth, Color(0xFF6366F1)),
            Triple("আমল শিক্ষা", Icons.Outlined.School, Color(0xFF8B5CF6)),
            Triple("রমজান", Icons.Outlined.ModeNight, Color(0xFF6366F1)),
            Triple("ইসলামিক নাম", Icons.Outlined.People, Color(0xFF3B82F6)),
            Triple("নামাজ শিক্ষা", Icons.Outlined.SelfImprovement, Color(0xFF14B8A6))
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        when (item.first) {
                            "আমল শিক্ষা", "নামাজ শিক্ষা", "Amal Learning", "Salah Learning" -> onNavigateToComingSoon()
                            "তাসবিহ", "Tasbih" -> onNavigateToTasbih()
                            "আল কুরআন", "Al Quran" -> onNavigateToQuran()
                            "যাকাত", "Zakat" -> onNavigateToZakat()
                            "ক্যালেন্ডার", "Calendar" -> onNavigateToCalendar()
                            "কিবলা", "Qibla" -> onNavigateToQibla()
                            "আল্লাহর নাম", "Allah's Names" -> onNavigateToAllahNames()
                            "রমজান", "Ramadan" -> onNavigateToRamadan()
                            "দোয়া", "Dua" -> onNavigateToDua()
                            "হাদিস", "Hadith" -> onNavigateToHadith()
                            "ইসলামিক নাম", "Islamic Name" -> onNavigateToIslamicNames()
                        }
                    }
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color(
                                red = (item.third.red * 0.82f).coerceIn(0f, 1f),
                                green = (item.third.green * 0.82f).coerceIn(0f, 1f),
                                blue = (item.third.blue * 0.82f).coerceIn(0f, 1f),
                                alpha = 1f
                            ), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.second, contentDescription = item.first, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.first,
                    color = TextDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ForbiddenTimeCard(title: String, start: String, end: String, icon: ImageVector, countdown: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = title, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(start, color = PrimaryGreen, fontSize = 11.sp)
        Text(if (GlobalLanguage.isEnglish) "from" else "থেকে", color = TextGray, fontSize = 10.sp)
        Text(end, color = PrimaryGreen, fontSize = 11.sp)
        if (countdown.isNotEmpty()) {
            Text(countdown, color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun SocialBlockerOverlay(
    platformName: String,
    onDismissToHome: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableStateOf(5) }
    
    val hadiths = remember {
        listOf(
            "“রাসূলুল্লাহ (সাঃ) বলেছেন: মানুষের ইসলামের অন্যতম সৌন্দর্য হলো নিরর্থক ও অনর্থক কথা ও কাজ পরিহার করা।” — তিরমিযী",
            "“রাসূলুল্লাহ (সাঃ) বলেছেন: যে ব্যক্তি আল্লাহ ও পরকালের প্রতি ঈমান রাখে, সে যেন ভালো কথা বলে অথবা নীরব থাকে।” — বুখারী",
            "“রাসূলুল্লাহ (সাঃ) বলেছেন: দুটি নিয়ামত এমন রয়েছে, যে দুটিতে অধিকাংশ মানুষ ক্ষতিগ্রস্ত; তা হলো স্বাস্থ্য এবং অবসর সময়।” — বুখারী",
            "“রাসূলুল্লাহ (সাঃ) বলেছেন: নিশ্চয়ই তোমার প্রতিপালকের প্রতি তোমার অবধারিত কতর্ব্য রয়েছে, তোমার নিজের প্রতিও কতর্ব্য রয়েছে।” — বুখারী",
            "“আল্লাহতায়ালা বলেছেন: আর তোমরা অনর্থক ক্রীড়াকৌতুক থেকে নিজেদের দূরে রাখো।” — সূরা আল-মুমিনুন"
        )
    }
    val selectedHadith = remember { hadiths.random() }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft--
        }
        onDismissToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Halal Circle App Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Halal Circle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // Warning Message
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "অবরুদ্ধ",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "অ্যাপ ব্যবহারে নিষেধাজ্ঞা",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "আপনি এখন $platformName ব্যবহার করতে পারবেন না। আপনার একাগ্রতা এবং আত্মিক উন্নয়ন বজায় রাখতে এই অ্যাপটি সাময়িকভাবে ব্লক করা হয়েছে।",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                // Beautiful Islamic Hadith Section with Rose/Muted Box styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF5FF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF3E8FF), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = selectedHadith,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF581C87),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Countdown Timer Progress Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF43F5E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = secondsLeft.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "সেকেন্ডের মধ্যে আপনাকে সড়িয়ে দেওয়া হবে...",
                        fontSize = 13.sp,
                        color = Color(0xFFF43F5E),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Exit immediately button
                Button(
                    onClick = onDismissToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "হোম স্কিনে ফিরে যান",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGrid(
    onNavigateToTracker: () -> Unit,
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToComingSoon: () -> Unit = {},
    onNavigateToQuran: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToAllahNames: () -> Unit = {},
    onNavigateToRamadan: () -> Unit = {},
    onNavigateToDuroodReminder: () -> Unit = {},
    onNavigateToDua: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToWidgets: () -> Unit = {},
    onNavigateToIslamicNames: () -> Unit = {},
    maxItems: Int? = null
) {
    val items = if (GlobalLanguage.isEnglish) {
        listOf(
            Triple("Al Quran", Icons.Outlined.MenuBook, PrimaryGreen),
            Triple("Hadith", Icons.Outlined.LibraryBooks, Color(0xFF3B82F6)),
            Triple("Tasbih", Icons.Outlined.Album, Color(0xFF8B5CF6)),
            Triple("Qibla", Icons.Outlined.Explore, PrimaryGreen),
            Triple("Dua", Icons.Outlined.WavingHand, Color(0xFFEC4899)),
            Triple("Allah's Names", Icons.Outlined.Star, Color(0xFFF59E0B)),
            Triple("Zakat", Icons.Outlined.MonetizationOn, PrimaryGreen),
            Triple("Calendar", Icons.Outlined.CalendarMonth, Color(0xFF6366F1)),
            Triple("Amal Learning", Icons.Outlined.School, Color(0xFF8B5CF6)),
            Triple("Ramadan", Icons.Outlined.ModeNight, Color(0xFF6366F1)),
            Triple("Islamic Name", Icons.Outlined.People, Color(0xFF3B82F6)),
            Triple("Salah Learning", Icons.Outlined.SelfImprovement, Color(0xFF14B8A6)),
            Triple("Durood Reminder", Icons.Outlined.Notifications, Color(0xFF8B5CF6)),
            Triple("Widgets", Icons.Outlined.Widgets, Color(0xFF10B982))
        )
    } else {
        listOf(
            Triple("আল কুরআন", Icons.Outlined.MenuBook, PrimaryGreen),
            Triple("হাদিস", Icons.Outlined.LibraryBooks, Color(0xFF3B82F6)),
            Triple("তাসবিহ", Icons.Outlined.Album, Color(0xFF8B5CF6)),
            Triple("কিবলা", Icons.Outlined.Explore, PrimaryGreen),
            Triple("দোয়া", Icons.Outlined.WavingHand, Color(0xFFEC4899)),
            Triple("আল্লাহর নাম", Icons.Outlined.Star, Color(0xFFF59E0B)),
            Triple("যাকাত", Icons.Outlined.MonetizationOn, PrimaryGreen),
            Triple("ক্যালেন্ডার", Icons.Outlined.CalendarMonth, Color(0xFF6366F1)),
            Triple("আমল শিক্ষা", Icons.Outlined.School, Color(0xFF8B5CF6)),
            Triple("রমজান", Icons.Outlined.ModeNight, Color(0xFF6366F1)),
            Triple("ইসলামিক নাম", Icons.Outlined.People, Color(0xFF3B82F6)),
            Triple("নামাজ শিক্ষা", Icons.Outlined.SelfImprovement, Color(0xFF14B8A6)),
            Triple("দরুদ রিমাইন্ডার", Icons.Outlined.Notifications, Color(0xFF8B5CF6)),
            Triple("উইজেট", Icons.Outlined.Widgets, Color(0xFF10B982))
        )
    }

    val displayItems = if (maxItems != null) items.take(maxItems) else items

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        val numRows = (displayItems.size + 3) / 4
        for (row in 0 until numRows) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..3) {
                    val index = row * 4 + col
                    if (index < displayItems.size) {
                        val item = displayItems[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (item.first == "আমল শিক্ষা" || item.first == "নামাজ শিক্ষা" || 
                                        item.first == "Amal Learning" || item.first == "Salah Learning") {
                                        onNavigateToComingSoon()
                                    } else if (item.first == "তাসবিহ" || item.first == "Tasbih") {
                                        onNavigateToTasbih()
                                    } else if (item.first == "আল কুরআন" || item.first == "Al Quran") {
                                        onNavigateToQuran()
                                    } else if (item.first == "যাকাত" || item.first == "Zakat") {
                                        onNavigateToZakat()
                                    } else if (item.first == "ক্যালেন্ডার" || item.first == "Calendar") {
                                        onNavigateToCalendar()
                                    } else if (item.first == "কিবলা" || item.first == "Qibla") {
                                        onNavigateToQibla()
                                    } else if (item.first == "আল্লাহর নাম" || item.first == "Allah's Names") {
                                        onNavigateToAllahNames()
                                    } else if (item.first == "রমজান" || item.first == "Ramadan") {
                                        onNavigateToRamadan()
                                    } else if (item.first == "দরুদ রিমাইন্ডার" || item.first == "Durood Reminder") {
                                        onNavigateToDuroodReminder()
                                    } else if (item.first == "দোয়া" || item.first == "Dua") {
                                        onNavigateToDua()
                                    } else if (item.first == "হাদিস" || item.first == "Hadith") {
                                        onNavigateToHadith()
                                    } else if (item.first == "উইজেট" || item.first == "Widgets") {
                                        onNavigateToWidgets()
                                    } else if (item.first == "ইসলামিক নাম" || item.first == "Islamic Name") {
                                        onNavigateToIslamicNames()
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(
                                            Color(
                                                red = (item.third.red * 0.82f).coerceIn(0f, 1f),
                                                green = (item.third.green * 0.82f).coerceIn(0f, 1f),
                                                blue = (item.third.blue * 0.82f).coerceIn(0f, 1f),
                                                alpha = 1f
                                            ), 
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.second, contentDescription = item.first, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.first,
                                color = TextDark,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ToolsScreen(
    onNavigateToTracker: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToComingSoon: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToZakat: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToAllahNames: () -> Unit,
    onNavigateToRamadan: () -> Unit,
    onNavigateToDuroodReminder: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onNavigateToIslamicNames: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (GlobalLanguage.isEnglish) "All Tools" else "সকল ক্যাটাগরি", 
                fontWeight = FontWeight.Bold, 
                color = TextDark, 
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Categories Grid
        CategoryGrid(
            onNavigateToTracker = onNavigateToTracker,
            onNavigateToTasbih = onNavigateToTasbih,
            onNavigateToComingSoon = onNavigateToComingSoon,
            onNavigateToQuran = onNavigateToQuran,
            onNavigateToZakat = onNavigateToZakat,
            onNavigateToCalendar = onNavigateToCalendar,
            onNavigateToQibla = onNavigateToQibla,
            onNavigateToAllahNames = onNavigateToAllahNames,
            onNavigateToRamadan = onNavigateToRamadan,
            onNavigateToDuroodReminder = onNavigateToDuroodReminder,
            onNavigateToDua = onNavigateToDua,
            onNavigateToHadith = onNavigateToHadith,
            onNavigateToWidgets = onNavigateToWidgets,
            onNavigateToIslamicNames = onNavigateToIslamicNames
        )
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { androidx.compose.animation.core.Animatable(0.5f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(800)
                )
            }
        }
        kotlinx.coroutines.delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-30).dp)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_app_logo_asset),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "From Circle",
                color = com.example.ui.theme.TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
