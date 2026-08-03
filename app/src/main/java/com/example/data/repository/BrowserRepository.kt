package com.example.data.repository

import com.example.ai.GeminiApiClient
import com.example.data.db.*
import com.example.data.model.NewsArticle
import com.example.data.model.QuickSite
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val historyDao: HistoryDao,
    private val bookmarkDao: BookmarkDao,
    private val downloadDao: DownloadDao,
    private val userSettingsDao: UserSettingsDao
) {
    val historyList: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val bookmarkList: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val downloadList: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()

    suspend fun addHistory(title: String, url: String, isSearch: Boolean = false) {
        if (url.isNotBlank() && !url.startsWith("about:blank")) {
            historyDao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url, isSearchQuery = isSearch))
        }
    }

    suspend fun clearHistory() = historyDao.clearHistory()
    suspend fun deleteHistory(id: Long) = historyDao.deleteHistory(id)

    suspend fun toggleBookmark(title: String, url: String): Boolean {
        val existing = bookmarkDao.getBookmarkByUrl(url)
        return if (existing != null) {
            bookmarkDao.deleteBookmark(existing.id)
            false
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(title = title, url = url))
            true
        }
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.getBookmarkByUrl(url) != null
    }

    suspend fun addBookmark(title: String, url: String, category: String = "সাধারণ") {
        bookmarkDao.insertBookmark(BookmarkEntity(title = title, url = url, category = category))
    }

    suspend fun deleteBookmark(id: Long) = bookmarkDao.deleteBookmark(id)

    suspend fun addDownloadLog(fileName: String, url: String, size: String = "অজানা আকার") {
        downloadDao.insertDownload(DownloadEntity(fileName = fileName, url = url, fileSize = size))
    }

    suspend fun clearDownloads() = downloadDao.clearDownloads()
    suspend fun deleteDownload(id: Long) = downloadDao.deleteDownload(id)

    suspend fun updateSettings(settings: UserSettingsEntity) {
        userSettingsDao.updateUserSettings(settings)
    }

    // AI Helper integrations
    suspend fun askGeminiAssistant(userPrompt: String, systemPrompt: String? = null): String {
        val defaultSystem = "তুমি জুনায়েদ AI ব্রাউজারের একজন অত্যন্ত বুদ্ধিমান ও বন্ধুভাবাপন্ন বাংলা AI সহকারী। ব্যবহারকারীর সমস্ত প্রশ্নের উত্তর সহজ, স্পষ্ট ও আকর্ষণীয় বাংলায় দাও।"
        return GeminiApiClient.askGemini(userPrompt, systemPrompt ?: defaultSystem)
    }

    suspend fun summarizePage(title: String, webText: String): String {
        val prompt = "নিচের ওয়েবসাইটের সংক্ষেপ বাংলায় ৩-৪ টি পয়েন্টে তুলে ধরো:\nশিরোনাম: $title\nকনটেন্ট: ${webText.take(2000)}"
        return askGeminiAssistant(prompt)
    }

    suspend fun explainSimply(webText: String): String {
        val prompt = "নিচের লেখাটি একদম সহজ ভাষায় একজন সাধারণ মানুষকে বুঝিয়ে বলো বাংলায়:\n${webText.take(2000)}"
        return askGeminiAssistant(prompt)
    }

    suspend fun translateToBengali(webText: String): String {
        val prompt = "নিচের তথ্যটি শুদ্ধ বাংলায় অনুবাদ করো:\n${webText.take(2000)}"
        return askGeminiAssistant(prompt)
    }

    // Default Bengali Quick Sites & Portals
    fun getPopularSites(): List<QuickSite> {
        return listOf(
            QuickSite("প্রথম আলো", "https://www.prothomalo.com", category = "সংবাদ"),
            QuickSite("গুগল", "https://www.google.com", category = "সার্চ"),
            QuickSite("ইউটিউব", "https://www.youtube.com", category = "ভিডিও"),
            QuickSite("ফেসবুক", "https://www.facebook.com", category = "সোশ্যাল"),
            QuickSite("উইকিপিডিয়া বাংলা", "https://bn.wikipedia.org", category = "শিক্ষা"),
            QuickSite("বিডিনিউজ২৪", "https://bangla.bdnews24.com", category = "সংবাদ"),
            QuickSite("বাংলাদেশ জাতীয় তথ্য বাতায়ন", "https://bangladesh.gov.bd", category = "সরকারী"),
            QuickSite("১০ মিনিট স্কুল", "https://10minuteschool.com", category = "শিক্ষা"),
            QuickSite("ক্রিকবাপ/ক্রিকইনফো", "https://www.espncricinfo.com", category = "খেলা"),
            QuickSite("সময় নিউজ", "https://www.somoynews.tv", category = "সংবাদ")
        )
    }

    // Sample Bengali News Feed for Home Screen
    fun getTrendingNews(): List<NewsArticle> {
        return listOf(
            NewsArticle(
                id = "1",
                title = "কৃত্রিম বুদ্ধিমত্তার নতুন যুগে প্রবেশ করলো বাংলা প্রযুক্তি বিশ্ব",
                summary = "জুনায়েদ AI ব্রাউজার নিয়ে এলো স্মার্ট ওয়েব সার্ফিং ও বাংলায় তাৎক্ষণিক AI বিশ্লেষণের নতুন অভিজ্ঞতা।",
                source = "টেক বাংলা",
                url = "https://www.prothomalo.com/technology",
                category = "টেকনোলজি",
                timeAgo = "১০ মিনিট আগে"
            ),
            NewsArticle(
                id = "2",
                title = "বাংলাদেশ দলের আসন্ন টি-টোয়েন্টি সিরিজের নতুন স্কোয়াড ঘোষণা",
                summary = "প্রধান নির্বাচক জানিয়েছেন তারুণ্য ও অভিজ্ঞতার সমন্বয়ে বিশ্বমানের টিম গঠন করা হয়েছে।",
                source = "বিডিনিউজ২৪",
                url = "https://bangla.bdnews24.com/cricket",
                category = "খেলাধুলা",
                timeAgo = "৩০ মিনিট আগে"
            ),
            NewsArticle(
                id = "3",
                title = "বিশ্বের সর্বাধুনিক স্মার্টসিটি ও ডিজিটাল গভর্ন্যান্স উদ্যোগ",
                summary = "ডিজিটাল বাংলাদেশের ধারাবাহিকতায় কৃত্রিম বুদ্ধিমত্তা চালিত প্রশাসনিক সেবা বাস্তবায়নের পদক্ষেপ।",
                source = "সময় নিউজ",
                url = "https://www.somoynews.tv/bangladesh",
                category = "বাংলাদেশ",
                timeAgo = "১ ঘণ্টা আগে"
            ),
            NewsArticle(
                id = "4",
                title = "জেমস ওয়েব টেলিস্কোপে মিললো দূরবর্তী ছায়াপথের মহাজাগতিক ছবি",
                summary = "বিজ্ঞানী দল নিশ্চিত করেছেন কোটি কোটি আলোকবর্ষ দূরের প্রাচীন তারাপুঞ্জ দৃশ্যমান হয়েছে।",
                source = "বিজ্ঞান চিন্তা",
                url = "https://bn.wikipedia.org/wiki/Science",
                category = "বিজ্ঞান",
                timeAgo = "২ ঘণ্টা আগে"
            ),
            NewsArticle(
                id = "5",
                title = "বাংলা চলচ্চিত্রের নতুন রেকর্ড: বিশ্বজুড়ে প্রশংসিত ঢাকাই সিনেমা",
                summary = "আন্তর্জাতিক চলচ্চিত্র উৎসবে বাংলাদেশ নির্মিত পূর্ণদৈর্ঘ্য চলচ্চিত্র লাভ করলো সর্বোচ্চ সম্মাননা।",
                source = "প্রথম আলো",
                url = "https://www.prothomalo.com/entertainment",
                category = "বিনোদন",
                timeAgo = "৩ ঘণ্টা আগে"
            )
        )
    }
}
