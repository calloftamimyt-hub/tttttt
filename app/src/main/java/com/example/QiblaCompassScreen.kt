package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isEng = GlobalLanguage.isEnglish

    // Calculate precision values to Mecca (Kaaba)
    val qiblaBearing = remember(latitude, longitude) {
        calculateQiblaBearing(latitude, longitude).toFloat()
    }
    val distanceToMecca = remember(latitude, longitude) {
        calculateDistanceToKaaba(latitude, longitude)
    }

    // Compass State
    var azimuth by remember { mutableFloatStateOf(0f) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }

    val rotationVectorSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    val accelerometerSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magneticSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    var gravity = remember { FloatArray(3) }
    var geomagnetic = remember { FloatArray(3) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    degree = (degree + 360) % 360
                    azimuth = degree
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        gravity = event.values.clone()
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        geomagnetic = event.values.clone()
                    }
                    if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(r, orientation)
                            var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            degree = (degree + 360) % 360
                            azimuth = degree
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager?.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Calculate relative alignment angle
    // relAngle is in range [-180, 180]
    val relAngle = remember(azimuth, qiblaBearing) {
        var rel = (qiblaBearing - azimuth) % 360
        if (rel > 180) rel -= 360
        if (rel < -180) rel += 360
        rel
    }

    val isAligned = remember(relAngle) {
        abs(relAngle) <= 4.0f
    }

    // Vibrate when phone becomes perfectly aligned
    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pulse animation for alignment glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isAligned) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEng) "Qibla Compass" else "কিবলা কম্পাস", 
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BgLight)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Location details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Location",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isEng) "Active Geolocation" else "সক্রিয় ভূ-অবস্থান",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = locationName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isEng) "Coordinates" else "স্থানাঙ্ক",
                                fontSize = 11.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.4f° N, %.4f° E", latitude, longitude),
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isEng) "Distance to Kaaba" else "কাবা থেকে দূরত্ব",
                                fontSize = 11.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEng) {
                                    String.format(java.util.Locale.US, "%,d km", distanceToMecca.roundToInt())
                                } else {
                                    convertNumberToBengali(String.format(java.util.Locale.US, "%,d", distanceToMecca.roundToInt())) + " কি.মি."
                                },
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guidance indicator banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isAligned) PrimaryGreen.copy(alpha = 0.15f)
                        else if (relAngle > 0) Color(0xFF3B82F6).copy(alpha = 0.1f)
                        else Color(0xFFEC4899).copy(alpha = 0.1f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isAligned) PrimaryGreen else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isAligned) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aligned",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isEng) "Aligned with Qibla! 🕋" else "কিবলাহ্ অভিমুখে রয়েছে! 🕋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )
                    } else {
                        val turnIcon = if (relAngle > 0) Icons.Default.ArrowForwardIos else Icons.Default.ArrowBackIosNew
                        val turnColor = if (relAngle > 0) Color(0xFF2563EB) else Color(0xFFDB2777)
                        
                        Icon(
                            imageVector = turnIcon,
                            contentDescription = "Rotate",
                            tint = turnColor,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Text(
                            text = if (isEng) {
                                if (relAngle > 0) "Turn Right (${abs(relAngle).roundToInt()}°)" else "Turn Left (${abs(relAngle).roundToInt()}°)"
                            } else {
                                if (relAngle > 0) "ডানে ঘুরুন (${convertNumberToBengali(abs(relAngle).roundToInt().toString())}°)"
                                else "বামে ঘুরুন (${convertNumberToBengali(abs(relAngle).roundToInt().toString())}°)"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = turnColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Compass Visual Element
            Box(
                modifier = Modifier
                    .size(290.dp * if (isAligned) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(CardBg)
                    .border(
                        width = if (isAligned) 6.dp else 2.dp,
                        color = if (isAligned) PrimaryGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                CompassDial(
                    azimuth = azimuth, 
                    qiblaBearing = qiblaBearing,
                    isAligned = isAligned
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Info Card & Calibration Tips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Calibration",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isEng) "Sensor Accuracy & Calibration" else "সেন্সর নির্ভুলতা ও ক্যালিব্রেট",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            text = if (isEng) {
                                "Keep your device flat on a level surface away from metallic or magnetic objects. If compass acts weirdly, wave it in a figure-8 motion."
                            } else {
                                "সঠিক ফলাফলের জন্য ফোনটি সমতল স্থানে রাখুন এবং ধাতব বস্তু থেকে দূরে রাখুন। কম্পাস ঠিকমতো কাজ না করলে বাতাসে '৮' অক্ষরের মতো ঘুরিয়ে ক্যালিব্রেট করুন।"
                            },
                            fontSize = 12.sp,
                            color = TextGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompassDial(
    azimuth: Float, 
    qiblaBearing: Float,
    isAligned: Boolean
) {
    val compassRotation = -azimuth
    val animatedRotation by animateFloatAsState(
        targetValue = compassRotation, 
        animationSpec = tween(120, easing = LinearOutSlowInEasing), 
        label = "compass"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val qiblaColor = PrimaryGreen
    val darkGrey = Color(0xFF1E293B)
    val lightGrey = Color(0xFF94A3B8)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(animatedRotation)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2
            
            // Outer Dial Ring ticks (Every 15 degrees)
            for (i in 0 until 360 step 15) {
                val angleRad = Math.toRadians(i.toDouble() - 90.0)
                val lineLength = if (i % 90 == 0) 14.dp.toPx() else 8.dp.toPx()
                val strokeW = if (i % 90 == 0) 2.5.dp.toPx() else 1.dp.toPx()
                val color = if (i % 90 == 0) darkGrey else lightGrey
                
                val startX = center.x + (radius - lineLength) * cos(angleRad).toFloat()
                val startY = center.y + (radius - lineLength) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()
                
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW
                )
            }
            
            // Cardinal direction labels (N, E, S, W)
            val cardinalDirs = listOf(
                Pair("N", 0f),
                Pair("E", 90f),
                Pair("S", 180f),
                Pair("W", 270f)
            )
            // Note: Canvas text drawing is done via nativeCanvas for performance and precision
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize = 14.dp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                cardinalDirs.forEach { (label, angle) ->
                    val angleRad = Math.toRadians(angle.toDouble() - 90.0)
                    val labelRadius = radius - 24.dp.toPx()
                    val x = center.x + labelRadius * cos(angleRad).toFloat()
                    val y = center.y + labelRadius * sin(angleRad).toFloat() + 5.dp.toPx()
                    
                    paint.color = if (label == "N") {
                        android.graphics.Color.RED
                    } else {
                        if (isDarkModeGlobal) android.graphics.Color.parseColor("#94A3B8") else android.graphics.Color.parseColor("#475569")
                    }
                    canvas.nativeCanvas.drawText(label, x, y, paint)
                }
            }

            // North Arrow pointer on the dial
            val nArrowPath = Path().apply {
                moveTo(center.x, center.y - radius + 32.dp.toPx())
                lineTo(center.x - 7.dp.toPx(), center.y - radius + 42.dp.toPx())
                lineTo(center.x + 7.dp.toPx(), center.y - radius + 42.dp.toPx())
                close()
            }
            drawPath(nArrowPath, color = Color.Red)

            // Draw Qibla line and Custom Kaaba Icon at qiblaBearing
            val qiblaRad = Math.toRadians(qiblaBearing.toDouble() - 90.0)
            val qStartX = center.x + (radius - 54.dp.toPx()) * cos(qiblaRad).toFloat()
            val qStartY = center.y + (radius - 54.dp.toPx()) * sin(qiblaRad).toFloat()
            val qEndX = center.x + (radius - 12.dp.toPx()) * cos(qiblaRad).toFloat()
            val qEndY = center.y + (radius - 12.dp.toPx()) * sin(qiblaRad).toFloat()
            
            drawLine(
                color = qiblaColor,
                start = Offset(qStartX, qStartY),
                end = Offset(qEndX, qEndY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Draw a high-quality stylized Kaaba vector symbol at the Qibla angle!
            val kX = center.x + (radius - 30.dp.toPx()) * cos(qiblaRad).toFloat()
            val kY = center.y + (radius - 30.dp.toPx()) * sin(qiblaRad).toFloat()
            
            rotate(degrees = qiblaBearing, pivot = Offset(kX, kY)) {
                val sizePx = 22.dp.toPx()
                // Kaaba cube body
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(kX - sizePx / 2, kY - sizePx / 2),
                    size = Size(sizePx, sizePx)
                )
                // Kaaba gold kiswah band
                drawRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(kX - sizePx / 2, kY - sizePx / 6),
                    size = Size(sizePx, 3.5.dp.toPx())
                )
                // Kaaba white base
                drawRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(kX - sizePx / 2, kY + sizePx / 2 - 2.dp.toPx()),
                    size = Size(sizePx, 2.dp.toPx())
                )
            }
        }
        
        // Fixed top phone direction indicator (always pointing straight up on screen)
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Heading",
            tint = if (isAligned) qiblaColor else primaryColor,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
        )
    }
}

private fun calculateQiblaBearing(lat: Double, lon: Double): Double {
    val latKaaba = Math.toRadians(21.422487)
    val lngKaaba = Math.toRadians(39.826206)
    
    val latLocal = Math.toRadians(lat)
    val lngLocal = Math.toRadians(lon)
    
    val deltaLng = lngKaaba - lngLocal
    
    val y = sin(deltaLng)
    val x = cos(latLocal) * tan(latKaaba) - sin(latLocal) * cos(deltaLng)
    
    var qiblaBearing = Math.toDegrees(atan2(y, x))
    qiblaBearing = (qiblaBearing + 360) % 360
    return qiblaBearing
}

private fun calculateDistanceToKaaba(lat: Double, lon: Double): Double {
    val latKaaba = 21.422487
    val lngKaaba = 39.826206
    val r = 6371.0 // Earth's radius in km
    
    val dLat = Math.toRadians(latKaaba - lat)
    val dLon = Math.toRadians(lngKaaba - lon)
    
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(latKaaba)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun convertNumberToBengali(input: String): String {
    val englishDigits = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bengaliDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()
    for (char in input) {
        val index = englishDigits.indexOf(char)
        if (index != -1) {
            builder.append(bengaliDigits[index])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}
