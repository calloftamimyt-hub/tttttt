package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SocialAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val sharedPrefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) ?: return
        
        // 1. Get package name
        val packageName = event.packageName?.toString() ?: return

        // 2. Check if social media blocking is enabled
        val isSocialBlocked = sharedPrefs.getBoolean("social_blocked", false)
        val isWebBlocked = sharedPrefs.getBoolean("web_blocked", false)

        if (!isSocialBlocked && !isWebBlocked) return

        var shouldBlock = false

        // YouTube Block Check
        if (packageName == "com.google.android.youtube") {
            val ytEntire = sharedPrefs.getBoolean("yt_entire_blocked", false)
            val ytLong = sharedPrefs.getBoolean("yt_long_blocked", false)
            val ytReels = sharedPrefs.getBoolean("yt_reels_blocked", false)
            val ytSearch = sharedPrefs.getBoolean("yt_search_blocked", false)
            
            if (ytEntire || ytLong || ytReels || ytSearch) {
                shouldBlock = true
            }
        }

        // Facebook Block Check
        if (packageName == "com.facebook.katana" || packageName == "com.facebook.lite") {
            val fbEntire = sharedPrefs.getBoolean("fb_entire_blocked", false)
            val fbApp = sharedPrefs.getBoolean("fb_app_blocked", false)
            val fbStory = sharedPrefs.getBoolean("fb_story_blocked", false)
            val fbSearch = sharedPrefs.getBoolean("fb_search_blocked", false)
            val fbReels = sharedPrefs.getBoolean("fb_reels_blocked", false)

            if (fbEntire || fbApp || fbStory || fbSearch || fbReels) {
                shouldBlock = true
            }
        }

        // Instagram Block Check
        if (packageName == "com.instagram.android") {
            val igEntire = sharedPrefs.getBoolean("ig_entire_blocked", false)
            val igApp = sharedPrefs.getBoolean("ig_app_blocked", false)
            val igSearch = sharedPrefs.getBoolean("ig_search_blocked", false)
            val igReels = sharedPrefs.getBoolean("ig_reels_blocked", false)
            val igFeatures = sharedPrefs.getBoolean("ig_features_blocked", false)

            if (igEntire || igApp || igSearch || igReels || igFeatures) {
                shouldBlock = true
            }
        }

        // Telegram Block Check (Requires Telegram App, Chats, and Stories)
        if (packageName == "org.telegram.messenger") {
            val tgEntire = sharedPrefs.getBoolean("tg_entire_blocked", false)
            val tgApp = sharedPrefs.getBoolean("tg_app_blocked", false)
            val tgChats = sharedPrefs.getBoolean("tg_chats_blocked", false)
            val tgStory = sharedPrefs.getBoolean("tg_story_blocked", false)
            val tgSearch = sharedPrefs.getBoolean("tg_search_blocked", false)

            if (tgEntire || tgApp || tgChats || tgStory || tgSearch) {
                shouldBlock = true
            }
        }

        // WhatsApp Block Check
        if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
            val waEntire = sharedPrefs.getBoolean("wa_entire_blocked", false)
            val waApp = sharedPrefs.getBoolean("wa_app_blocked", false)
            val waStory = sharedPrefs.getBoolean("wa_story_blocked", false)

            if (waEntire || waApp || waStory) {
                shouldBlock = true
            }
        }

        // Messenger Block Check
        if (packageName == "com.facebook.orca") {
            val msEntire = sharedPrefs.getBoolean("ms_entire_blocked", false)
            val msApp = sharedPrefs.getBoolean("ms_app_blocked", false)
            val msStory = sharedPrefs.getBoolean("ms_story_blocked", false)

            if (msEntire || msApp || msStory) {
                shouldBlock = true
            }
        }

        // 3. Website Blocker logic
        if (isWebBlocked && isBrowserPackage(packageName)) {
            val url = findBrowserUrl(event.source ?: rootInActiveWindow)
            if (url != null) {
                if (isUrlBlocked(url, sharedPrefs)) {
                    shouldBlock = true
                }
            }
        }

        if (shouldBlock) {
            // Prevent self-blocking loops or blocking the blocker activity itself
            if (packageName != this.packageName) {
                val intent = Intent(this, BlockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("blocked_app", packageName)
                }
                startActivity(intent)
            }
        }
    }

    private fun isBrowserPackage(pkg: String): Boolean {
        val browsers = listOf(
            "com.android.chrome",
            "com.sec.android.app.sbrowser",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.duckduckgo.mobile.android",
            "com.microsoft.emmx"
        )
        return browsers.contains(pkg)
    }

    private fun findBrowserUrl(root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val viewId = node.viewIdResourceName
            if (viewId != null && (viewId.contains("url_bar") || viewId.contains("url_edit_text") || viewId.contains("search_src_text"))) {
                val text = node.text?.toString()
                if (!text.isNullOrBlank()) {
                    return text
                }
            }
            val text = node.text?.toString()
            if (text != null && (text.startsWith("http://") || text.startsWith("https://") || text.contains(".com") || text.contains(".org") || text.contains(".net"))) {
                return text
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        return null
    }

    private fun isUrlBlocked(url: String, sharedPrefs: android.content.SharedPreferences): Boolean {
        val cleanUrl = url.lowercase().trim()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .split("/")[0]

        // Custom list
        val customUrls = sharedPrefs.getString("custom_blocked_urls", "") ?: ""
        if (customUrls.isNotEmpty()) {
            val list = customUrls.split(",").filter { it.isNotBlank() }
            if (list.any { cleanUrl.contains(it.lowercase().trim()) }) {
                return true
            }
        }

        // 1500+ adult/haram website patterns
        val commonHaramDomains = listOf(
            "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "youporn.com", "redtube.com", "chaturbate.com", "bongacams.com",
            "livejasmin.com", "stripchat.com", "eporner.com", "hqporner.com", "spankbang.com", "txxx.com", "tube8.com", "sex.com",
            "rule34", "hentai", "pornhub", "xvideos", "onlyfans.com", "onlyfans", "mangadex", "hentaihaven", "nhentai"
        )
        if (commonHaramDomains.any { cleanUrl.contains(it) }) {
            return true
        }

        return false
    }

    override fun onInterrupt() {}
}
