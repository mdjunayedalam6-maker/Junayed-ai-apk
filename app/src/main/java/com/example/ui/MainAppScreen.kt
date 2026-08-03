package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import java.util.Locale

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavDestination("home", "হোম", Icons.Default.Home)
    object Browser : NavDestination("browser", "ব্রাউজার", Icons.Default.Language)
    object AiAssistant : NavDestination("ai_assistant", "এআই সহকারী", Icons.Default.AutoAwesome)
    object Bookmarks : NavDestination("bookmarks", "বুকমার্ক", Icons.Default.Bookmark)
    object History : NavDestination("history", "ইতিহাস", Icons.Default.History)
    object Downloads : NavDestination("downloads", "ডাউনলোড", Icons.Default.Download)
    object Settings : NavDestination("settings", "সেটিংস", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Home) }
    var isTabsOverviewOpen by remember { mutableStateOf(false) }

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val searchEngine by viewModel.currentSearchEngine.collectAsStateWithLifecycle()
    val urlInputText by viewModel.urlInput.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiSummaryText by viewModel.aiPageSummary.collectAsStateWithLifecycle()

    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val bookmarkList by viewModel.bookmarkList.collectAsStateWithLifecycle()
    val downloadList by viewModel.downloadList.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val isBookmarked = remember(activeTab.url, bookmarkList) {
        bookmarkList.any { it.url == activeTab.url }
    }

    // Voice Search Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                if (currentDestination == NavDestination.AiAssistant) {
                    viewModel.sendAiPrompt(spokenText!!)
                } else {
                    viewModel.openUrl(spokenText!!)
                    currentDestination = NavDestination.Browser
                }
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় বলুন...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognizer not supported on emulator/device
        }
    }

    if (isTabsOverviewOpen) {
        TabOverviewScreen(
            tabs = tabs,
            activeTabId = activeTabId,
            onTabSelected = { id ->
                viewModel.switchTab(id)
                currentDestination = NavDestination.Browser
            },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = {
                viewModel.createNewTab()
                currentDestination = NavDestination.Browser
            },
            onCloseOverview = { isTabsOverviewOpen = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val bottomNavItems = listOf(
                        NavDestination.Home,
                        NavDestination.Browser,
                        NavDestination.AiAssistant,
                        NavDestination.Bookmarks,
                        NavDestination.Settings
                    )

                    bottomNavItems.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination.route == dest.route,
                            onClick = { currentDestination = dest },
                            icon = { Icon(dest.icon, contentDescription = dest.title) },
                            label = { Text(dest.title) },
                            modifier = Modifier.testTag("nav_${dest.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    NavDestination.Home -> HomeScreen(
                        onSearchSubmitted = { query ->
                            viewModel.openUrl(query)
                            currentDestination = NavDestination.Browser
                        },
                        onQuickSiteClicked = { url ->
                            viewModel.openUrl(url)
                            currentDestination = NavDestination.Browser
                        },
                        onNewsArticleClicked = { url ->
                            viewModel.openUrl(url)
                            currentDestination = NavDestination.Browser
                        },
                        onOpenAiAssistant = {
                            currentDestination = NavDestination.AiAssistant
                        },
                        onVoiceSearchRequested = { launchVoiceSearch() },
                        searchEngine = searchEngine,
                        onSelectSearchEngine = { viewModel.setSearchEngine(it) },
                        popularSites = viewModel.popularSites,
                        trendingNews = viewModel.trendingNews
                    )

                    NavDestination.Browser -> BrowserScreen(
                        activeTab = activeTab,
                        tabsCount = tabs.size,
                        urlInputText = urlInputText,
                        onUrlInputChanged = { viewModel.updateUrlInput(it) },
                        onUrlSubmitted = { viewModel.openUrl(it) },
                        onProgressChange = { progress ->
                            viewModel.updateActiveTabState(progress = progress)
                        },
                        onTitleChange = { title ->
                            viewModel.updateActiveTabState(title = title)
                        },
                        onUrlChange = { url ->
                            viewModel.updateActiveTabState(url = url)
                        },
                        onPageTextCaptured = { text ->
                            viewModel.updateCapturedText(text)
                        },
                        onDownloadTriggered = { fileName, downloadUrl ->
                            viewModel.addDownload(fileName, downloadUrl, "মেগাবাইট")
                        },
                        onGoHome = {
                            viewModel.goHome()
                            currentDestination = NavDestination.Home
                        },
                        onOpenTabsOverview = { isTabsOverviewOpen = true },
                        onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                        onToggleIncognito = { viewModel.toggleIncognito() },
                        onToggleBookmark = { viewModel.toggleCurrentBookmark() },
                        isBookmarked = isBookmarked,
                        onOpenAiSheet = { currentDestination = NavDestination.AiAssistant },
                        onSummarizePage = { viewModel.summarizeCurrentPage() },
                        onExplainPage = { viewModel.explainCurrentPageSimply() },
                        onTranslatePage = { viewModel.translateCurrentPage() },
                        aiSummaryText = aiSummaryText,
                        isAiLoading = isAiLoading,
                        onClearAiSummary = { viewModel.clearAiSummary() }
                    )

                    NavDestination.AiAssistant -> AiAssistantScreen(
                        messages = aiMessages,
                        isLoading = isAiLoading,
                        onSendMessage = { prompt -> viewModel.sendAiPrompt(prompt) },
                        onVoiceInputRequested = { launchVoiceSearch() }
                    )

                    NavDestination.Bookmarks -> BookmarksScreen(
                        bookmarks = bookmarkList,
                        onOpenUrl = { url ->
                            viewModel.openUrl(url)
                            currentDestination = NavDestination.Browser
                        },
                        onDeleteBookmark = { id -> viewModel.removeBookmark(id) }
                    )

                    NavDestination.History -> HistoryScreen(
                        historyList = historyList,
                        onOpenUrl = { url ->
                            viewModel.openUrl(url)
                            currentDestination = NavDestination.Browser
                        },
                        onDeleteHistoryItem = { id -> viewModel.deleteHistoryItem(id) },
                        onClearAllHistory = { viewModel.clearHistory() }
                    )

                    NavDestination.Downloads -> DownloadsScreen(
                        downloadList = downloadList,
                        onDeleteDownload = { id -> viewModel.deleteDownload(id) },
                        onClearAllDownloads = { viewModel.clearDownloads() }
                    )

                    NavDestination.Settings -> ProfileAndSettingsScreen(
                        userSettings = userSettings ?: com.example.data.db.UserSettingsEntity(),
                        onSaveSettings = { viewModel.updateUserSettings(it) }
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.isEmpty()
