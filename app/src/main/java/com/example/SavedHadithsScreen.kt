package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.example.data.HadithData
import com.example.model.Hadith
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedHadithsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("hadith_bookmarks", Context.MODE_PRIVATE) }
    
    // State to trigger recomposition when un-saving
    var updateTrigger by remember { mutableStateOf(0) }
    
    val savedHadiths = remember(updateTrigger) {
        HadithData.hadithList.filter { sharedPrefs.getBoolean("hadith_${it.id}", false) }
    }
    
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Text(
                    text = "সংরক্ষিত হাদিস",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (savedHadiths.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.BookmarkAdded,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "কোনো সংরক্ষিত হাদিস নেই",
                            color = TextGray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(savedHadiths) { hadith ->
                        SavedHadithListItem(
                            hadith = hadith,
                            onUnsave = {
                                sharedPrefs.edit().putBoolean("hadith_${hadith.id}", false).apply()
                                updateTrigger++
                            },
                            onClick = { selectedHadith = hadith }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                }
            }
            
            selectedHadith?.let { hadith ->
                HadithDetailDialog(hadith = hadith, onDismiss = { selectedHadith = null })
            }
        }
    }
}

@Composable
fun SavedHadithListItem(
    hadith: Hadith,
    onUnsave: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Slim ID indicator
        Text(
            text = hadith.id,
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hadith.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = hadith.category,
                fontSize = 11.sp,
                color = TextGray
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onUnsave,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkAdded,
                contentDescription = "Unsave Hadith",
                tint = PrimaryGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
