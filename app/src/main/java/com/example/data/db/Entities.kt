package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSearchQuery: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val category: String = "সাধারণ",
    val createdAt: Long = System.currentTimeMillis(),
    val faviconUrl: String = ""
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val fileSize: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "সম্পন্ন",
    val mimeType: String = "file/*"
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "জুনায়েদ ব্যবহারকারী",
    val userEmail: String = "user@junaidbrowser.ai",
    val userPhone: String = "+8801700000000",
    val language: String = "bn", // "bn", "en"
    val themeMode: String = "system", // "system", "dark", "light"
    val searchEngine: String = "Google", // "Google", "Bing", "DuckDuckGo", "Wikipedia Bangla"
    val isSafeBrowsingEnabled: Boolean = true,
    val isDesktopModeDefault: Boolean = false,
    val isIncognitoDefault: Boolean = false
)
