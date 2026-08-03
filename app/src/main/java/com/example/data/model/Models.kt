package com.example.data.model

data class QuickSite(
    val title: String,
    val url: String,
    val iconResName: String = "",
    val category: String = "জনপ্রিয়",
    val description: String = ""
)

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val url: String,
    val category: String, // "বাংলাদেশ", "টেকনোলজি", "খেলাধুলা", "বিনোদন", "বিশ্ব", "বিজ্ঞান"
    val timeAgo: String,
    val imageUrl: String = ""
)

data class TabItem(
    val id: String,
    val title: String = "নতুন ট্যাব",
    val url: String = "about:blank",
    val faviconUrl: String = "",
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val isHome: Boolean = true
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
