package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    url: String,
    isDesktopMode: Boolean,
    isIncognito: Boolean,
    onProgressChange: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onFaviconChange: (String) -> Unit,
    onPageTextCaptured: (String) -> Unit,
    onDownloadTriggered: (String, String) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val defaultMobileUserAgent = remember {
        "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
    val desktopUserAgent = remember {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    userAgentString = if (isDesktopMode) desktopUserAgent else defaultMobileUserAgent
                }

                if (isIncognito) {
                    CookieManager.getInstance().setAcceptCookie(false)
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                } else {
                    CookieManager.getInstance().setAcceptCookie(true)
                }

                setDownloadListener { downloadUrl, _, contentDisposition, mimeType, _ ->
                    val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                    onDownloadTriggered(fileName, downloadUrl)
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChange(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        if (!title.isNullOrEmpty()) {
                            onTitleChange(title!!)
                        }
                    }

                    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                        // Favicon captured
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (customView != null) {
                            callback?.onCustomViewHidden()
                            return
                        }
                        customView = view
                        customViewCallback = callback
                    }

                    override fun onHideCustomView() {
                        customViewCallback?.onCustomViewHidden()
                        customView = null
                        customViewCallback = null
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (url != null) {
                            onUrlChange(url)
                        }
                    }

                    override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                        if (finishedUrl != null) {
                            onUrlChange(finishedUrl)
                            // Extract page text for Gemini AI analysis
                            view?.evaluateJavascript(
                                "(function() { return document.body ? document.body.innerText : ''; })();"
                            ) { result ->
                                if (result != null && result != "null") {
                                    val unquoted = result.removeSurrounding("\"").replace("\\n", "\n")
                                    onPageTextCaptured(unquoted)
                                }
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val reqUrl = request?.url?.toString() ?: return false
                        if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                            return false
                        }
                        return true
                    }
                }

                onWebViewCreated(this)
                if (url.isNotBlank() && url != "about:blank") {
                    loadUrl(url)
                }
            }
        },
        update = { webView ->
            webView.settings.userAgentString = if (isDesktopMode) desktopUserAgent else defaultMobileUserAgent
            if (url.isNotBlank() && url != "about:blank" && webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.isEmpty()
