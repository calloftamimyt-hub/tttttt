package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.example.data.DuaData
import com.example.data.DuaStorage
import com.example.model.Dua
import com.example.ui.theme.*
import com.example.viewmodel.GlobalLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDuasScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isEng = GlobalLanguage.isEnglish
    val duaStorage = remember { DuaStorage(context) }

    // Re-trigger load state when bookmarks change
    var savedIds by remember { mutableStateOf(duaStorage.getAllSavedIds()) }

    val savedDuas = remember(savedIds) {
        DuaData.duaList.filter { savedIds.contains(it.id) }
    }

    var selectedDua by remember { mutableStateOf<Dua?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEng) "Saved Duas" else "সেভ করা দোয়া",
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (savedDuas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEng) "No Saved Duas Yet" else "কোনো দোয়া সেভ করা নেই",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isEng) {
                            "Tap the bookmark button in the Dua section to save Duas here."
                        } else {
                            "দোয়ার ক্যাটাগরি থেকে পছন্দের দোয়ার পাশে সেভ বাটনে চাপ দিলে তা এখানে জমা হবে।"
                        },
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedDuas) { dua ->
                        SavedDuaCard(
                            dua = dua,
                            onClick = { selectedDua = dua },
                            onUnsave = {
                                duaStorage.toggleSavedDua(dua.id)
                                savedIds = duaStorage.getAllSavedIds()
                            },
                            isEng = isEng
                        )
                    }
                }
            }

            // Detail Dialog
            selectedDua?.let { dua ->
                DuaDetailDialog(
                    dua = dua,
                    onDismiss = { selectedDua = null }
                )
            }
        }
    }
}

@Composable
fun SavedDuaCard(
    dua: Dua,
    onClick: () -> Unit,
    onUnsave: () -> Unit,
    isEng: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dua.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .background(PrimaryGreen.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                IconButton(
                    onClick = onUnsave,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmark,
                        contentDescription = "Unsave",
                        tint = Color(0xFFF59E0B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dua.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dua.translation,
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
