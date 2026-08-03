package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserSettingsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsScreen(
    userSettings: UserSettingsEntity,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var userName by remember(userSettings) { mutableStateOf(userSettings.userName) }
    var userEmail by remember(userSettings) { mutableStateOf(userSettings.userEmail) }
    var userPhone by remember(userSettings) { mutableStateOf(userSettings.userPhone) }
    var selectedLanguage by remember(userSettings) { mutableStateOf(userSettings.language) }
    var searchEngine by remember(userSettings) { mutableStateOf(userSettings.searchEngine) }
    var isSafeBrowsing by remember(userSettings) { mutableStateOf(userSettings.isSafeBrowsingEnabled) }
    var isDesktopDefault by remember(userSettings) { mutableStateOf(userSettings.isDesktopModeDefault) }
    var isIncognitoDefault by remember(userSettings) { mutableStateOf(userSettings.isIncognitoDefault) }

    var showEngineMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        TopAppBar(
            title = { Text("প্রোফাইল ও সেটিংস", fontWeight = FontWeight.Bold) }
        )

        // User Profile Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = userEmail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Account Details Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "ব্যবহারকারীর তথ্য",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    onSaveSettings(userSettings.copy(userName = it))
                },
                label = { Text("নাম") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userEmail,
                onValueChange = {
                    userEmail = it
                    onSaveSettings(userSettings.copy(userEmail = it))
                },
                label = { Text("ইমেইল") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userPhone,
                onValueChange = {
                    userPhone = it
                    onSaveSettings(userSettings.copy(userPhone = it))
                },
                label = { Text("ফোন নম্বর") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            HorizontalDivider()

            // Browser Preference Settings
            Text(
                text = "ব্রাউজার সেটিংস",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Search Engine Setting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ডিফল্ট সার্চ ইঞ্জিন", fontSize = 14.sp)
                }

                Box {
                    TextButton(onClick = { showEngineMenu = true }) {
                        Text(searchEngine, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showEngineMenu,
                        onDismissRequest = { showEngineMenu = false }
                    ) {
                        listOf("Google", "Bing", "DuckDuckGo", "Wikipedia Bangla").forEach { engine ->
                            DropdownMenuItem(
                                text = { Text(engine) },
                                onClick = {
                                    searchEngine = engine
                                    onSaveSettings(userSettings.copy(searchEngine = engine))
                                    showEngineMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Safe Browsing Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("নিরাপদ ব্রাউজিং (Safe Browsing)", fontSize = 14.sp)
                        Text("ক্ষতিকারক ওয়েবসাইটের নিরাপত্তা সতর্কতা", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Switch(
                    checked = isSafeBrowsing,
                    onCheckedChange = {
                        isSafeBrowsing = it
                        onSaveSettings(userSettings.copy(isSafeBrowsingEnabled = it))
                    }
                )
            }

            // Desktop Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DesktopWindows, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ডিফল্ট ডেস্কটপ মোড", fontSize = 14.sp)
                }

                Switch(
                    checked = isDesktopDefault,
                    onCheckedChange = {
                        isDesktopDefault = it
                        onSaveSettings(userSettings.copy(isDesktopModeDefault = it))
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // App About
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Junaid AI Browser v1.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("বিশ্বের ইন্টারনেট, এখন আপনার স্মার্ট বাংলা ব্রাউজারে।", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("চালিত: Google Gemini AI Technology", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
