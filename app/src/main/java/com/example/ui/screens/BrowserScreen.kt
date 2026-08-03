package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.TabItem
import com.example.ui.components.BrowserWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    activeTab: TabItem,
    tabsCount: Int,
    urlInputText: String,
    onUrlInputChanged: (String) -> Unit,
    onUrlSubmitted: (String) -> Unit,
    onProgressChange: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onPageTextCaptured: (String) -> Unit,
    onDownloadTriggered: (String, String) -> Unit,
    onGoHome: () -> Unit,
    onOpenTabsOverview: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleIncognito: () -> Unit,
    onToggleBookmark: () -> Unit,
    isBookmarked: Boolean,
    onOpenAiSheet: () -> Unit,
    onSummarizePage: () -> Unit,
    onExplainPage: () -> Unit,
    onTranslatePage: () -> Unit,
    aiSummaryText: String?,
    isAiLoading: Boolean,
    onClearAiSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top URL Bar
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onGoHome, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    // Address Bar
                    OutlinedTextField(
                        value = urlInputText,
                        onValueChange = onUrlInputChanged,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = if (activeTab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (activeTab.isIncognito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activeTab.progress in 1..99) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = { webViewInstance?.reload() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Reload",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("browser_url_input")
                    )

                    // Tab Counter
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onOpenTabsOverview() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$tabsCount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Menu Options
                    Box {
                        IconButton(onClick = { showMenuDropdown = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = showMenuDropdown,
                            onDismissRequest = { showMenuDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isBookmarked) "বুকমার্ক থেকে মুছুন" else "বুকমার্ক করুন") },
                                leadingIcon = {
                                    Icon(
                                        if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onToggleBookmark()
                                    showMenuDropdown = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (activeTab.isDesktopMode) "মোবাইল সাইট" else "ডেস্কটপ সাইট") },
                                leadingIcon = {
                                    Icon(
                                        if (activeTab.isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.DesktopWindows,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onToggleDesktopMode()
                                    showMenuDropdown = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (activeTab.isIncognito) "সাধারণ মোড" else "প্রাইভেট মোড") },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
                                onClick = {
                                    onToggleIncognito()
                                    showMenuDropdown = false
                                }
                            )
                        }
                    }
                }

                // Page Loading Progress Line
                if (activeTab.progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { activeTab.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Quick AI Floating Bar Options
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onSummarizePage,
                    label = { Text("সারসংক্ষেপ", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                )

                AssistChip(
                    onClick = onExplainPage,
                    label = { Text("সহজ ব্যাখ্যা", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                )

                AssistChip(
                    onClick = onTranslatePage,
                    label = { Text("বাংলা অনুবাদ", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }

        // AI Generated Page Summary Pop-Up Banner
        AnimatedVisibility(
            visible = aiSummaryText != null,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Gemini AI বিশ্লেষণ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(onClick = onClearAiSummary, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isAiLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI প্রক্রিয়াধীন রয়েছে...", fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = aiSummaryText.orEmpty(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Main WebView Canvas
        Box(modifier = Modifier.weight(1f)) {
            BrowserWebView(
                url = activeTab.url,
                isDesktopMode = activeTab.isDesktopMode,
                isIncognito = activeTab.isIncognito,
                onProgressChange = onProgressChange,
                onTitleChange = onTitleChange,
                onUrlChange = onUrlChange,
                onFaviconChange = {},
                onPageTextCaptured = onPageTextCaptured,
                onDownloadTriggered = onDownloadTriggered,
                onWebViewCreated = { webViewInstance = it }
            )
        }

        // Bottom Controls Bar
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    enabled = activeTab.canGoBack
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (activeTab.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.goForward() },
                    enabled = activeTab.canGoForward
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (activeTab.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                // Center AI Launcher Floating Fab
                FloatingActionButton(
                    onClick = onOpenAiSheet,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("browser_ai_fab")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", modifier = Modifier.size(22.dp))
                }

                IconButton(onClick = onOpenTabsOverview) {
                    Icon(Icons.Default.Tab, contentDescription = "Tabs")
                }

                IconButton(onClick = onGoHome) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    }
}
