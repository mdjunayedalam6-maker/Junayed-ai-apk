package com.example.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.HistoryEntity
import com.example.data.db.UserSettingsEntity
import com.example.data.model.AiChatMessage
import com.example.data.model.NewsArticle
import com.example.data.model.QuickSite
import com.example.data.model.TabItem
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BrowserRepository(
            historyDao = db.historyDao(),
            bookmarkDao = db.bookmarkDao(),
            downloadDao = db.downloadDao(),
            userSettingsDao = db.userSettingsDao()
        )
    }

    // Settings & Persistence
    val historyList: StateFlow<List<HistoryEntity>> = repository.historyList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkList: StateFlow<List<BookmarkEntity>> = repository.bookmarkList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadList = repository.downloadList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    // Tabs Management
    private val _tabs = MutableStateFlow<List<TabItem>>(listOf(
        TabItem(id = "tab_1", title = "হোম - জুনায়েদ AI ব্রাউজার", isHome = true)
    ))
    val tabs: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow("tab_1")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<TabItem> = combine(_tabs, _activeTabId) { tabList, activeId ->
        tabList.find { it.id == activeId } ?: tabList.firstOrNull() ?: TabItem(id = "fallback", isHome = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TabItem(id = "tab_1", isHome = true))

    // Search Engine
    private val _currentSearchEngine = MutableStateFlow("Google")
    val currentSearchEngine: StateFlow<String> = _currentSearchEngine.asStateFlow()

    // URL / Search Input State
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    // AI Assistant Messages
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage(
            message = "আসসালামু আলাইকুম! আমি জুনায়েদ AI সহকারী। আপনাকে কী সাহায্য করতে পারি? যেকোনো প্রশ্ন বাংলায় জিজ্ঞেস করুন বা চলতি পেজের সারসংক্ষেপ নিন।",
            isUser = false
        )
    ))
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiPageSummary = MutableStateFlow<String?>(null)
    val aiPageSummary: StateFlow<String?> = _aiPageSummary.asStateFlow()

    // WebView Current Page Text Capture
    private val _capturedWebText = MutableStateFlow("")
    val capturedWebText: StateFlow<String> = _capturedWebText.asStateFlow()

    // Content Feeds
    val popularSites: List<QuickSite> = repository.getPopularSites()
    val trendingNews: List<NewsArticle> = repository.getTrendingNews()

    fun updateUrlInput(text: String) {
        _urlInput.value = text
    }

    fun updateCapturedText(text: String) {
        _capturedWebText.value = text
    }

    fun setSearchEngine(engine: String) {
        _currentSearchEngine.value = engine
    }

    // Navigation & Tab Actions
    fun openUrl(input: String) {
        var formattedUrl = input.trim()
        if (formattedUrl.isEmpty()) return

        val isSearch = !Patterns.WEB_URL.matcher(formattedUrl).matches() && !formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")
        
        if (isSearch) {
            val encoded = URLEncoder.encode(formattedUrl, "UTF-8")
            formattedUrl = when (_currentSearchEngine.value) {
                "Bing" -> "https://www.bing.com/search?q=$encoded"
                "DuckDuckGo" -> "https://duckduckgo.com/?q=$encoded"
                "Wikipedia Bangla" -> "https://bn.wikipedia.org/w/index.php?search=$encoded"
                else -> "https://www.google.com/search?q=$encoded"
            }
        } else if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }

        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) {
                    tab.copy(url = formattedUrl, isHome = false, title = formattedUrl)
                } else tab
            }
        }
        _urlInput.value = formattedUrl

        viewModelScope.launch {
            repository.addHistory(title = formattedUrl, url = formattedUrl, isSearch = isSearch)
        }
    }

    fun updateActiveTabState(
        title: String? = null,
        url: String? = null,
        faviconUrl: String? = null,
        progress: Int? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null
    ) {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) {
                    val newUrl = url ?: tab.url
                    val newTitle = title ?: tab.title
                    if (url != null && url != tab.url && !url.startsWith("about:blank")) {
                        _urlInput.value = url
                        viewModelScope.launch {
                            repository.addHistory(title = newTitle, url = newUrl)
                        }
                    }
                    tab.copy(
                        title = newTitle,
                        url = newUrl,
                        faviconUrl = faviconUrl ?: tab.faviconUrl,
                        progress = progress ?: tab.progress,
                        canGoBack = canGoBack ?: tab.canGoBack,
                        canGoForward = canGoForward ?: tab.canGoForward,
                        isHome = newUrl.isEmpty() || newUrl == "about:blank"
                    )
                } else tab
            }
        }
    }

    fun createNewTab(url: String = "about:blank") {
        val newId = "tab_${System.currentTimeMillis()}"
        val isHome = url == "about:blank" || url.isEmpty()
        val newTab = TabItem(id = newId, url = url, title = if (isHome) "হোম - জুনায়েদ AI ব্রাউজার" else url, isHome = isHome)
        _tabs.update { it + newTab }
        _activeTabId.value = newId
        _urlInput.value = if (isHome) "" else url
    }

    fun closeTab(id: String) {
        val list = _tabs.value
        if (list.size <= 1) {
            // Keep at least 1 home tab
            _tabs.value = listOf(TabItem(id = "tab_home_${System.currentTimeMillis()}", isHome = true))
            _activeTabId.value = _tabs.value.first().id
            _urlInput.value = ""
            return
        }

        val newList = list.filter { it.id != id }
        _tabs.value = newList
        if (_activeTabId.value == id) {
            _activeTabId.value = newList.last().id
            _urlInput.value = if (newList.last().isHome) "" else newList.last().url
        }
    }

    fun switchTab(id: String) {
        _activeTabId.value = id
        val tab = _tabs.value.find { it.id == id }
        if (tab != null) {
            _urlInput.value = if (tab.isHome) "" else tab.url
        }
    }

    fun goHome() {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) {
                    tab.copy(url = "about:blank", title = "হোম - জুনায়েদ AI ব্রাউজার", isHome = true)
                } else tab
            }
        }
        _urlInput.value = ""
    }

    fun toggleDesktopMode() {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) {
                    tab.copy(isDesktopMode = !tab.isDesktopMode)
                } else tab
            }
        }
    }

    fun toggleIncognito() {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) {
                    tab.copy(isIncognito = !tab.isIncognito)
                } else tab
            }
        }
    }

    // AI Functions
    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(message = prompt, isUser = true)
        _aiMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val responseText = repository.askGeminiAssistant(prompt)
            val aiMsg = AiChatMessage(message = responseText, isUser = false)
            _aiMessages.update { it + aiMsg }
            _isAiLoading.value = false
        }
    }

    fun summarizeCurrentPage() {
        val tab = activeTab.value
        val pageText = _capturedWebText.value
        if (pageText.isBlank() && tab.isHome) {
            _aiPageSummary.value = "অনুগ্রহ করে একটি ওয়েবসাইট লোড করে পুনরায় সংক্ষেপ চেষ্টা করুন।"
            return
        }

        _isAiLoading.value = true
        _aiPageSummary.value = "Gemini AI ওয়েব পেজের বিষয়বস্তু বিশ্লেষণ করছে..."

        viewModelScope.launch {
            val summary = repository.summarizePage(title = tab.title, webText = pageText.ifBlank { tab.url })
            _aiPageSummary.value = summary
            _isAiLoading.value = false
            
            // Also append to AI chat log
            val summaryChat = AiChatMessage(message = "📄 **${tab.title}** এর সংক্ষেপ:\n\n$summary", isUser = false)
            _aiMessages.update { it + summaryChat }
        }
    }

    fun explainCurrentPageSimply() {
        val tab = activeTab.value
        val pageText = _capturedWebText.value
        _isAiLoading.value = true
        _aiPageSummary.value = "Gemini AI সহজ ভাষায় ব্যাখ্যা তৈরি করছে..."

        viewModelScope.launch {
            val explanation = repository.explainSimply(pageText.ifBlank { tab.title + " " + tab.url })
            _aiPageSummary.value = explanation
            _isAiLoading.value = false
            _aiMessages.update { it + AiChatMessage(message = "💡 **সহজ ব্যাখ্যা:**\n\n$explanation", isUser = false) }
        }
    }

    fun translateCurrentPage() {
        val tab = activeTab.value
        val pageText = _capturedWebText.value
        _isAiLoading.value = true

        viewModelScope.launch {
            val translation = repository.translateToBengali(pageText.ifBlank { tab.title })
            _aiPageSummary.value = translation
            _isAiLoading.value = false
            _aiMessages.update { it + AiChatMessage(message = "🌐 **বাংলা অনুবাদ:**\n\n$translation", isUser = false) }
        }
    }

    fun clearAiSummary() {
        _aiPageSummary.value = null
    }

    // Bookmarks & Downloads Actions
    fun toggleCurrentBookmark() {
        val tab = activeTab.value
        if (tab.url.isBlank() || tab.isHome) return
        viewModelScope.launch {
            repository.toggleBookmark(title = tab.title, url = tab.url)
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun addDownload(fileName: String, url: String, size: String) {
        viewModelScope.launch {
            repository.addDownloadLog(fileName, url, size)
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            repository.clearDownloads()
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}
